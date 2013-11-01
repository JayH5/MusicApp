package za.jamie.soundstage.pablo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import za.jamie.soundstage.utils.AppUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.squareup.picasso.Cache;

public class DiskCache implements Cache {

	private static final String TAG = "DiskCache";
	
	public static final String DEFAULT_CACHE_DIR_NAME = "Cache";
	public static final int DEFAULT_COMPRESS_QUALITY = 98;

	private final File mCacheDir;
	
	private final Object mLock = new Object();
	
	public DiskCache(Context context) {
		this(AppUtils.getCacheDir(context, DEFAULT_CACHE_DIR_NAME));
	}
	
	public DiskCache(File directory) {
		mCacheDir = directory;
	}
	
	private boolean createDirIfNeeded() {
		synchronized (mLock) {
			if (mCacheDir == null) {
				return false;
			}
			if (!mCacheDir.exists()) {
				return mCacheDir.mkdirs();
			}
			return true;
		}
	}

	@Override
	public void set(String key, Bitmap bitmap) {		
		synchronized (mLock) {
			if (createDirIfNeeded()) {
				final String diskKey = hashKeyForDisk(key);
				File file = new File(mCacheDir, diskKey);
				if (file.exists()) { // Already have file saved
					return;
				}
				OutputStream out = null;
				try {
					out = new FileOutputStream(file);
					bitmap.compress(CompressFormat.JPEG, DEFAULT_COMPRESS_QUALITY, out);
				} catch (FileNotFoundException e) {
					Log.e(TAG, "Cache file could not be created.", e);
				} finally {
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {}
					}
				}
			}
		}
	}
	
	public static void writeStreams(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		for (int len; (len = in.read(buffer)) != -1;) {
			out.write(buffer, 0, len);
		}
	}

	@Override
	public Bitmap get(String key) {
		Bitmap bitmap = null;
		final String diskKey = hashKeyForDisk(key);
		synchronized(mLock) {
			//if (createDirIfNeeded()) {
				File cacheFile = new File(mCacheDir, diskKey);
				bitmap = BitmapFactory.decodeFile(cacheFile.getPath());
			//}
		}
		return bitmap;
	}
	
	public boolean delete(String key) {
		final String diskKey = hashKeyForDisk(key);	
		synchronized(mLock) {
			if (createDirIfNeeded()) {
				File cacheFile = new File(mCacheDir, diskKey);
				return cacheFile.delete();
			}
		}
		return false;
	}

	@Override
	public void clear() {
		synchronized (mLock) {
			if (createDirIfNeeded()) {
				for (File file : mCacheDir.listFiles()) {
					file.delete();
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
    public static String hashKeyForDisk(String key) {
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
		return 1;
	}

	@Override
	public int size() {
		return 0; // Infinite size
	}

}
