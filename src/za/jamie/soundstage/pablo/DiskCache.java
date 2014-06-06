package za.jamie.soundstage.pablo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.StatFs;
import android.util.Log;

import com.squareup.okhttp.internal.DiskLruCache;
import com.squareup.picasso.Cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import za.jamie.soundstage.utils.AppUtils;

public class DiskCache implements Cache {

	private static final String TAG = "DiskCache";

    private static final int MIN_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_SIZE = 50 * 1024 * 1024; // 50MB

    private static final String DEFAULT_CACHE_DIR_NAME = "imagecache";
	private static final CompressFormat COMPRESS_FORMAT = CompressFormat.JPEG;
    private static final int COMPRESS_QUALITY = 95;
    private static final int INDEX = 0;

    private static final int MESSAGE_CLEAR = 0;
    private static final int MESSAGE_INIT = 1;
    private static final int MESSAGE_FLUSH = 2;
    private static final int MESSAGE_CLOSE = 3;

	private DiskLruCache mCache;
    private File mCacheDir;

	private final Object mLock = new Object();
    private boolean mStarting = true;

	public DiskCache(Context context) {
		this(AppUtils.getAppDir(context, DEFAULT_CACHE_DIR_NAME));
	}

	public DiskCache(File directory) {
		mCacheDir = directory;
        new CacheAsyncTask().execute(MESSAGE_INIT);
	}

	private void init() {
        synchronized (mLock) {
            if (mCache == null || mCache.isClosed()) {
                if (!mCacheDir.exists()) {
                    mCacheDir.mkdirs();
                }
                final long size = calculateDiskCacheSize(mCacheDir);
                if (size > 0) {
                    try {
                        mCache = DiskLruCache.open(mCacheDir, 1, 1, size);
                    } catch (final IOException e) {
                        Log.e(TAG, "init - " + e);
                    }
                }
            }
            mStarting = false;
            mLock.notifyAll();
        }
    }

    @Override
	public void set(String data, Bitmap bitmap) {
        synchronized (mLock) {
            // Add to disk cache
            if (mCache != null) {
                final String key = hashKeyForDisk(data);
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = mCache.get(key);
                    if (snapshot == null) {
                        final DiskLruCache.Editor editor = mCache.edit(key);
                        if (editor != null) {
                            out = editor.newOutputStream(INDEX);
                            bitmap.compress(COMPRESS_FORMAT, COMPRESS_QUALITY, out);
                            editor.commit();
                            out.close();
                        }
                    } else {
                        snapshot.getInputStream(INDEX).close();
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "set - " + e);
                } catch (Exception e) {
                    Log.e(TAG, "set - " + e);
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {}
                }
            }
        }
	}

	@Override
	public Bitmap get(String data) {
		final String key = hashKeyForDisk(data);
        Bitmap bitmap = null;

        synchronized (mLock) {
            while (mStarting) {
                try {
                    mLock.wait();
                } catch (InterruptedException e) {}
            }
            if (mCache != null) {
                InputStream inputStream = null;
                try {
                    final DiskLruCache.Snapshot snapshot = mCache.get(key);
                    if (snapshot != null) {
                        inputStream = snapshot.getInputStream(INDEX);
                        if (inputStream != null) {
                            bitmap = BitmapFactory.decodeStream(inputStream);
                        }
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "get - " + e);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {}
                }
            }
            return bitmap;
        }
	}

	@Override
	public void clear() {
        new CacheAsyncTask().execute(MESSAGE_CLEAR);
	}

    private void clearInternal() {
        synchronized (mLock) {
            mStarting = true;
            if (mCache != null && !mCache.isClosed()) {
                try {
                    mCache.delete();
                } catch (IOException e) {
                    Log.e(TAG, "clear - " + e);
                }
                mCache = null;
                init();
            }
        }
    }

    public void flush() {
        new CacheAsyncTask().execute(MESSAGE_FLUSH);
    }

    private void flushInternal() {
        synchronized (mLock) {
            if (mCache != null) {
                try {
                    mCache.flush();
                } catch (IOException e) {
                    Log.e(TAG, "flush - " + e);
                }
            }
        }
    }

    public void close() {
        new CacheAsyncTask().execute(MESSAGE_CLOSE);
    }

    private void closeInternal() {
        synchronized (mLock) {
            if (mCache != null) {
                try {
                    if (!mCache.isClosed()) {
                        mCache.close();
                        mCache = null;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "close - " + e);
                }
            }
        }
    }

	/**
     * A hashing method that changes a string (like a URL) into a hash suitable
     * for using as a disk filename.
     *
     * @param key The key used to store the file
     */
    private static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(key.getBytes());
            cacheKey = bytesToHexString(digest.digest());
        } catch (final NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
            Log.w(TAG, "MD5 hashing algorithm not found. Falling back to Object.hashCode()");
        }
        return cacheKey;
    }

    /**
     * http://stackoverflow.com/questions/332079
     *
     * @param bytes The bytes to convert.
     * @return A {@link String} converted from the bytes of a hashable key used
     *         to store a filename on the disk, to hex digits.
     */
    private static String bytesToHexString(byte[] bytes) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            final String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }

	@Override
	public int maxSize() {
        synchronized (mLock) {
            if (mCache != null && !mCache.isClosed()) {
                return (int) mCache.getMaxSize();
            }
        }
        return 0;
	}

	@Override
	public int size() {
        synchronized (mLock) {
            if (mCache != null && !mCache.isClosed()) {
                return (int) mCache.size();
            }
        }
		return 0;
	}

    private static long calculateDiskCacheSize(File dir) {
        long size = MIN_SIZE;

        try {
            StatFs statFs = new StatFs(dir.getAbsolutePath());
            long available = ((long) statFs.getBlockCount()) * statFs.getBlockSize();
            // Target 10% of the total space.
            size = available / 10;
        } catch (IllegalArgumentException ignored) {
        }

        // Bound inside min/max size for disk cache.
        return Math.max(Math.min(size, MAX_SIZE), MIN_SIZE);
    }

    private class CacheAsyncTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            switch (params[0]) {
                case MESSAGE_CLEAR:
                    clearInternal();
                    break;
                case MESSAGE_INIT:
                    init();
                    break;
                case MESSAGE_FLUSH:
                    flushInternal();
                    break;
                case MESSAGE_CLOSE:
                    closeInternal();
                    break;
            }
            return null;
        }
    }

}
