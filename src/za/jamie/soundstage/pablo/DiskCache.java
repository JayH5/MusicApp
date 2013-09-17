package za.jamie.soundstage.pablo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import za.jamie.soundstage.utils.AppUtils;
import android.content.Context;
import android.util.Log;

public class DiskCache {

	private static final String TAG = "DiskCache";
	
	public static final String DEFAULT_CACHE_DIR_NAME = "Cache";

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

	public void put(String key, InputStream in) {		
		synchronized (mLock) {
			if (createDirIfNeeded()) {
				final String diskKey = hashKeyForDisk(key);
				File file = new File(mCacheDir, diskKey);
				OutputStream out = null;
				try {
					out = new FileOutputStream(file);
					writeStreams(in, out);
				} catch (FileNotFoundException e) {
					Log.e(TAG, "Cache file could not be created.", e);
				} catch (IOException e) {
					Log.e(TAG, "Error adding bitmap to cache.", e);
					// Delete file if there was a problem writing it
					file.delete();
				} finally {
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {}
					}
					if (in != null) {
						try {
							in.close();
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

	public InputStream get(String key) {
		final String diskKey = hashKeyForDisk(key);		
		synchronized(mLock) {
			if (createDirIfNeeded()) {
				File cacheFile = new File(mCacheDir, diskKey);
				try {
					return new FileInputStream(cacheFile);
				} catch (FileNotFoundException ignored) { }
			}
		}
		return null;
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

}
