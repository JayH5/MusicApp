/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.jamie.soundstage.bitmapfun;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import za.jamie.soundstage.utils.AppUtils;
import android.app.Activity;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.jakewharton.DiskLruCache;

/**
 * This class holds our bitmap caches (memory and disk).
 */
public class ImageCache {
    private static final String TAG = "ImageCache";

    // Default memory cache size
    private static final float MEM_CACHE_DIVIDER = 0.25f; // 25%

    // Default disk cache parameters
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final int DISK_CACHE_INDEX = 0;
    private static final CompressFormat COMPRESS_FORMAT = CompressFormat.JPEG;
    private static final int COMPRESS_QUALITY = 90;
    
    private static final String DEFAULT_CACHE_DIR = "ImageCache";
    private static final boolean DEFAULT_MEM_ENABLED = true;

    private DiskLruCache mDiskCache;
    private LruCache<String, Bitmap> mMemoryCache;
    
    private static ImageCache sInstance;

    /**
     * Creating a new ImageCache object using the specified parameters. This constructor
     * should not be called directly. Rather use findoOrCreateCache().
     *
     * @param context The context to use
     * @param cacheParams The cache parameters to use to initialize the cache
     */
    public ImageCache(Context context, String cacheDir, boolean memCache) {
        if (memCache) {
        	mMemoryCache = createMemoryCache(context);
        	registerComponentCallbacks2(context);
        }
        initDiskCache(context, cacheDir);
    }
    
    public static ImageCache getInstance(Context context) {
    	
    	if (sInstance == null) {
    		sInstance = new ImageCache(context.getApplicationContext(), 
    				DEFAULT_CACHE_DIR, DEFAULT_MEM_ENABLED);
    	}
    	return sInstance;
    }
    
    public ImageCache(Activity activity, String cacheDir, boolean memCache) {
    	if (memCache) {
    		mMemoryCache = findOrCreateMemoryCache(activity);
    		registerComponentCallbacks2(activity);
    	}
    	initDiskCache(activity, cacheDir);
    }
    
    private void registerComponentCallbacks2(Context context) {
    	context.registerComponentCallbacks(new ComponentCallbacks2() {
            
			@Override
            public void onTrimMemory(final int level) {
                if (level >= TRIM_MEMORY_MODERATE) {
                    mMemoryCache.evictAll();
                } else if (level >= TRIM_MEMORY_BACKGROUND) {
                    mMemoryCache.trimToSize(mMemoryCache.size() / 2);
                }
            }

			@Override
			public void onConfigurationChanged(Configuration newConfig) {}

			@Override
			public void onLowMemory() {}
    	});
    }

    /**
     * Find and return an existing LruCache stored in a {@link RetainFragment}, if not found a new
     * one is created using the supplied params and saved to a {@link RetainFragment}.
     *
     * @param activity The calling {@link FragmentActivity}
     */
    @SuppressWarnings("unchecked")
	private static LruCache<String, Bitmap> findOrCreateMemoryCache(Activity activity) {

        LruCache<String, Bitmap> memoryCache = null;
    	// Search for, or create an instance of the non-UI RetainFragment
    	final RetainFragment retainFragment = RetainFragment.findOrCreateRetainFragment(
    			activity.getFragmentManager());
        	
    	// See if we already have an LruCache stored in RetainFragment
    	memoryCache = (LruCache<String, Bitmap>) retainFragment.getObject();
        	
        if (memoryCache == null) {
        	// No existing LruCache, create one and store it in RetainFragment
        	Log.d(TAG, "Couldn't restore mem cache from retain frag. Creating new instance.");
        	memoryCache = createMemoryCache(activity);
        	retainFragment.setObject(memoryCache);
        }
        
        return memoryCache;
    }
    
    /**
     * Initializes the disk cache. Note that this includes disk access so this
     * should not be executed on the main/UI thread. By default an ImageCache
     * does not initialize the disk cache when it is created, instead you should
     * call initDiskCache() to initialize it on a background thread.
     * 
     * @param context The {@link Context} to use
     */
    private void initDiskCache(final Context context, final String cacheDir) {        
        // Get the actual file for the cache
        final File diskCacheDir = AppUtils.getCacheDir(context, cacheDir);
        
        // Initialize the disk cache in a background thread
        if (mDiskCache == null || mDiskCache.isClosed() || 
        		!mDiskCache.getDirectory().equals(diskCacheDir)) {
        	
        	Log.d(TAG, "Creating disk cache");
        	
        	new AsyncTask<File, Void, DiskLruCache>() {

        		@Override
        		protected DiskLruCache doInBackground(File... params) {
        			return createDiskCache(params[0]);
        		}
        		
        		@Override
        		protected void onPostExecute(DiskLruCache result) {
        			mDiskCache = result;
        		}
        		
        	}.execute(diskCacheDir);
        }
    }
    
    private static DiskLruCache createDiskCache(File diskCacheDir) {
    	if (diskCacheDir != null) {
    		if (!diskCacheDir.exists()) {
                diskCacheDir.mkdirs();
            }
    		if (diskCacheDir.getUsableSpace() > DISK_CACHE_SIZE) {
                try {
                    return DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
                } catch (final IOException ioe) {
                	Log.e(TAG, "Error during opening of DiskLruCache.", ioe);
                }
            }
    	}
    	return null;
    }

    /**
     * Sets up the Lru cache
     * 
     * @param context The {@link Context} to use
     */
    
    private static LruCache<String, Bitmap> createMemoryCache(final Context context) {
        return new LruCache<String, Bitmap>(AppUtils.calculateMemCacheSize(context, 
        		MEM_CACHE_DIVIDER)) {
            
        	// Measure item size in bytes rather than units
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };

    }

    /**
     * Adds a new image to the memory and disk caches
     * 
     * @param data The key used to store the image
     * @param bitmap The {@link Bitmap} to cache
     */
    public void addBitmapToCache(final String data, final Bitmap bitmap) {
        if (data == null || bitmap == null) {
            return;
        }

       	addBitmapToMemCache(data, bitmap);
        addBitmapToDiskCache(data, bitmap);
    }

    /**
     * Called to add a new image to the memory cache
     * 
     * @param data The key identifier
     * @param bitmap The {@link Bitmap} to cache
     */
    public void addBitmapToMemCache(final String data, final Bitmap bitmap) {
        if (mMemoryCache != null) {
        	synchronized (mMemoryCache) {
        		if (mMemoryCache.get(data) == null) {
        			mMemoryCache.put(data, bitmap);
        		}
        	}
        }       
    }
    
    public void addBitmapToDiskCache(final String data, final Bitmap bitmap) {
    	if (mDiskCache != null) {
    		 //synchronized(mDiskCache) {
    			final String key = hashKeyForDisk(data);
    			OutputStream out = null;
    			try {
    				final DiskLruCache.Snapshot snapshot = mDiskCache.get(key);
    				if (snapshot == null) {
    					final DiskLruCache.Editor editor = mDiskCache.edit(key);
    					if (editor != null) {
    						out = editor.newOutputStream(DISK_CACHE_INDEX);
    						bitmap.compress(COMPRESS_FORMAT, COMPRESS_QUALITY, out);
    						editor.commit();
    						out.close();
    					}
    				} else {
    					snapshot.getInputStream(DISK_CACHE_INDEX).close();
    				}
    			} catch (final IOException ioe) {
    				Log.e(TAG, "Error adding bitmap to disk cache.", ioe);
    			} finally {
    				if (out != null) {
    					 try {
    						out.close();
    					} catch (final IOException ioe) {
        					Log.e(TAG, "Error closing output stream (addBitmapToDiskCache)", ioe);
        				}
    				} 
    			}
    		//}
    	}
    }
    
    /**
     * @param key The key used to identify which cache entries to delete.
     */
    public void removeFromCache(final String key) {
        if (key == null) {
            return;
        }
        
        if (mMemoryCache != null) {
        	mMemoryCache.remove(key);
        }
        if (mDiskCache != null) {
        	//synchronized (mDiskCache) {
        		try {
        			mDiskCache.remove(key);
        			
        			//if (!mDiskCache.isClosed()) {
            			//mDiskCache.flush();
            		//}
        		} catch (IOException e) {
        			Log.e(TAG, "Failed to remove file from disk cache.", e);
        		}
        	//}
        }
    }
    
    /**
     * A hashing method that changes a string (like a URL) into a hash suitable
     * for using as a disk filename.
     * 
     * @param key The key used to store the file
     */
    public static final String hashKeyForDisk(final String key) {
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
    private static final String bytesToHexString(final byte[] bytes) {
        final StringBuilder builder = new StringBuilder();
        for (final byte b : bytes) {
            final String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }

    public Bitmap getBitmapFromCache(String data) {
    	Bitmap bitmap = getBitmapFromMemCache(data);
    	if (bitmap == null) {
    		bitmap = getBitmapFromDiskCache(data);
    	}
    	return bitmap;
    }
    
    /**
     * Get from memory cache.
     *
     * @param data Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    public Bitmap getBitmapFromMemCache(String data) {
    	if (data != null && mMemoryCache != null) {
        	return mMemoryCache.get(data);
        }
        return null;
    }

    /**
     * Fetches a cached image from the disk cache
     * 
     * @param data Unique identifier for which item to get
     * @return The {@link Bitmap} if found in cache, null otherwise
     */
    public final Bitmap getBitmapFromDiskCache(final String data) {
        if (data != null && mDiskCache != null) {
        	//synchronized (mDiskCache) {
        		final String key = hashKeyForDisk(data);
        		InputStream inputStream = null;
        		try {
        			final DiskLruCache.Snapshot snapshot = mDiskCache.get(key);
        			if (snapshot != null) {
        				inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
        				if (inputStream != null) {
        					return BitmapFactory.decodeStream(inputStream);
        				}
        			}
        		} catch (final IOException ioe) {
        			Log.e(TAG, "Error while fetching bitmap from disk cache.", ioe);
        		} finally {
        			if (inputStream != null) {
        				try {
        					inputStream.close();
        				} catch (final IOException ioe) {
            				Log.w(TAG, "Failed to close input stream.", ioe);
            			}
        			} 
        		}
        	//}
        }
        return null;
    }

    /**
     * Clears the disk and memory caches
     */
    public void clearCaches() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final Void... unused) {
                // Clear the disk cache
                try {
                    if (mDiskCache != null) {
                        mDiskCache.delete();
                        mDiskCache.close();
                    }
                } catch (final IOException ioe) {
                    Log.e(TAG, "Error while clearing caches.", ioe);
                }
                return null;
            }
        }.execute();
        
        // Clear the memory cache
        if (mMemoryCache != null) {
        	mMemoryCache.evictAll();
        }
    }
    
    public void clearMemoryCache() {
    	if (mMemoryCache != null) {
    		mMemoryCache.evictAll();
    	}
    }
    
    public void close() {
    	if (mMemoryCache != null) {
    		mMemoryCache.evictAll();
    	}
    	
    	if (mDiskCache != null) {
			try {
				mDiskCache.close();
			} catch(IOException e) {
				Log.e(TAG, "Error closing DiskLruCache -- LEAK", e);
			} finally {
				mDiskCache = null;
			}
    	}
    }
}
