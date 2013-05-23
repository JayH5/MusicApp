package za.jamie.soundstage.bitmapfun;

import java.io.File;
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

import com.jakewharton.disklrucache.DiskLruCache;

public class DiskCache implements Cache<String, Bitmap> {

	private static final String TAG = "DiskCache";
	
	private DiskLruCache mCache;
	private CacheParams mParams;
	
	public DiskCache(Context context, String cacheDir) {
		mParams = new CacheParams();
		init(context, cacheDir);
	}
	
	public DiskCache(Context context, String cacheDir, CacheParams params) {
		mParams = params;
		init(context, cacheDir);
	}
	
	private void init(Context context, String cacheDir) {
		File cacheFile = getDiskCacheFile(context, cacheDir);
		try {
			mCache = DiskLruCache.open(cacheFile, 1, 1, mParams.size);
		} catch (IOException e) {
			Log.e(TAG, "Error opening disk cache.", e);
		}
	}
	
	private File getDiskCacheFile(Context context, String cacheDir) {
		final File diskCacheDir = AppUtils.getCacheDir(context, cacheDir);
		if (diskCacheDir != null) {
			if (!diskCacheDir.exists()) {
                diskCacheDir.mkdirs();
            }
			if (diskCacheDir.getUsableSpace() > mParams.size) {
				return diskCacheDir;
			}
		}
		return null;
	}
	
	public CacheParams getCacheParams() {
		return mParams;
	}

	@Override
	public void put(String key, Bitmap value) {
		final String diskKey = hashKeyForDisk(key);
		OutputStream out = null;
		synchronized(this) {
			try {
				// First check if the bitmap is already in the cache
				final DiskLruCache.Snapshot snapshot = mCache.get(diskKey);
				if (snapshot == null) {
					// If not, get an editor to add the bitmap to the cache
					final DiskLruCache.Editor editor = mCache.edit(diskKey);
					if (editor != null) {
						out = editor.newOutputStream(mParams.index);
						value.compress(mParams.compressFormat, mParams.compressQuality, out);
						editor.commit();
						out.close();
					}
				} else {
					snapshot.close();
				}
			} catch (IOException e) {
				Log.e(TAG, "Error adding bitmap to cache.", e);
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						Log.e(TAG, "Error closing output.", e);
					}
				}
			}
		}
	}
	
	@Override
	public void remove(String key) {
		final String diskKey = hashKeyForDisk(key);
		try {
			mCache.remove(diskKey);
		} catch (IOException e) {
			Log.e(TAG, "Error removing bitmap from cache.", e);
		}
	}

	@Override
	public Bitmap get(String key) {
		final String diskKey = hashKeyForDisk(key);
		InputStream in = null;
		try {
			final DiskLruCache.Snapshot snapshot = mCache.get(diskKey);
			if (snapshot != null) {
				in = snapshot.getInputStream(mParams.index);
				if (in != null) {
					return BitmapFactory.decodeStream(in);
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "Error getting bitmap from cache.", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					Log.e(TAG, "Error closing stream getting bitmap from cache.", e);
				}
			}
		}
		return null;
	}

	@Override
	public void clear() {
		try {
			mCache.delete();
		} catch (IOException e) {
			Log.e(TAG, "Error clearing disk cache.", e);
		}		
	}

	@Override
	public void close() {
		try {
			mCache.close();
		} catch (IOException e) {
			Log.e(TAG, "Error closing disk cache.", e);
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
	
	public static class CacheParams {
		private int size = 1024 * 1024 * 10; // 10MB
		private int index = 0;
		private CompressFormat compressFormat = CompressFormat.JPEG;
		private int compressQuality = 90;
		
		public CacheParams setSize(int size) {
			this.size = size;
			return this;
		}
		
		public CacheParams setIndex(int index) {
			this.index = index;
			return this;
		}
		
		public CacheParams setCompressFormat(CompressFormat format) {
			this.compressFormat = format;
			return this;
		}
		
		public CacheParams setCompressQuality(int quality) {
			this.compressQuality = quality;
			return this;
		}
	}
}
