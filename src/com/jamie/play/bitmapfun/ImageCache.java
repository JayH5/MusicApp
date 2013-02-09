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

package com.jamie.play.bitmapfun;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.LruCache;

import com.jakewharton.DiskLruCache;
import com.jamie.play.utils.AppUtils;
//import com.jamie.play.BuildConfig;

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

    private DiskLruCache mDiskCache;
    private LruCache<String, Bitmap> mMemoryCache;

    /**
     * Creating a new ImageCache object using the specified parameters.
     *
     * @param context The context to use
     * @param cacheParams The cache parameters to use to initialize the cache
     */
    public ImageCache(Context context, String cacheDir) {
        init(context, cacheDir);
    }
    
    /**
     * Creates a new ImageCache object synchronously and only with a disk cache.
     * For use by asynchronous services.
     * @param service
     */
    public ImageCache(Service service, String cacheDir) {
    	initDiskCache(service, cacheDir);
    }

    /**
     * Find and return an existing ImageCache stored in a {@link RetainFragment}, if not found a new
     * one is created using the supplied params and saved to a {@link RetainFragment}.
     *
     * @param activity The calling {@link FragmentActivity}
     * @param cacheDir The directory for the disk cache
     * @return An existing retained ImageCache object or a new one if one did not exist
     */
    public static ImageCache findOrCreateCache(Activity activity, String cacheDir) {

        // Search for, or create an instance of the non-UI RetainFragment
        final RetainFragment mRetainFragment = RetainFragment.findOrCreateRetainFragment(
                activity.getFragmentManager());

        // See if we already have an ImageCache stored in RetainFragment
        ImageCache imageCache = (ImageCache) mRetainFragment.getObject();

        // No existing ImageCache, create one and store it in RetainFragment
        if (imageCache == null) {
        	Log.d(TAG, "Failed to restore cache from retain frag");
            imageCache = new ImageCache(activity, cacheDir);
            mRetainFragment.setObject(imageCache);
        } else {
        	Log.d(TAG, "Restored cache from retain frag");
        }

        return imageCache;
    }
    
    /**
     * Initializes the disk cache. Note that this includes disk access so this
     * should not be executed on the main/UI thread. By default an ImageCache
     * does not initialize the disk cache when it is created, instead you should
     * call initDiskCache() to initialize it on a background thread.
     * 
     * @param context The {@link Context} to use
     */
    private void init(final Context context, final String cacheDir) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final Void... unused) {
                // Initialize the disk cache in a background thread
                initDiskCache(context, cacheDir);
                return null;
            }
        }.execute((Void[])null);
        // Set up the memory cache
        initMemoryCache(context);
    }
    
    /**
     * Initializes the disk cache. Note that this includes disk access so this
     * should not be executed on the main/UI thread. By default an ImageCache
     * does not initialize the disk cache when it is created, instead you should
     * call initDiskCache() to initialize it on a background thread.
     * 
     * @param context The {@link Context} to use
     */
    public void initDiskCache(Context context, String cacheDir) {
        // Set up disk cache
        if (mDiskCache == null || mDiskCache.isClosed()) {
            File diskCacheDir = AppUtils.getCacheDir(context, cacheDir);
            if (diskCacheDir != null) {
                if (!diskCacheDir.exists()) {
                    diskCacheDir.mkdirs();
                }
                if (diskCacheDir.getUsableSpace() > DISK_CACHE_SIZE) {
                    try {
                        mDiskCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
                    } catch (final IOException e) {
                        diskCacheDir = null;
                    }
                }
            }
        }
    }

    /**
     * Sets up the Lru cache
     * 
     * @param context The {@link Context} to use
     */
    
    public void initMemoryCache(final Context context) {
        mMemoryCache = new LruCache<String, Bitmap>(AppUtils.calculateMemCacheSize(context, 
        		MEM_CACHE_DIVIDER)) {
            
        	// Measure item size in bytes rather than units
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };

        context.registerComponentCallbacks(new ComponentCallbacks2() {
        
        	@SuppressLint("NewApi")
			@Override
            public void onTrimMemory(final int level) {
                if (level >= TRIM_MEMORY_MODERATE) {
                    mMemoryCache.evictAll();
                } else if (level >= TRIM_MEMORY_BACKGROUND) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    	mMemoryCache.trimToSize(mMemoryCache.size() / 2);
                    }
                }
            }

			@Override
			public void onConfigurationChanged(Configuration newConfig) {}

			@Override
			public void onLowMemory() {}
        });
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
    private void addBitmapToMemCache(final String data, final Bitmap bitmap) {
        if (mMemoryCache == null) {
        	Log.e(TAG, "Memory cache is null!");
        	return;
        }
        
        // Add to memory cache
    	if (getBitmapFromMemCache(data) == null) {
    		mMemoryCache.put(data, bitmap);
    	}
    }
    
    private void addBitmapToDiskCache(final String data, final Bitmap bitmap) {
    	if (mDiskCache != null) {
    		 synchronized(mDiskCache) {
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
    						flush();
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
    		}
    	}
    }
    
    /**
     * @param key The key used to identify which cache entries to delete.
     */
    public void removeFromCache(final String key) {
        if (key == null) {
            return;
        }
        removeFromMemCache(key);
        removeFromDiskCache(key);
    }
    
    public void removeFromMemCache(String key) {
    	if (mMemoryCache != null) {
    		mMemoryCache.remove(key);
    	}
    }
    
    public void removeFromDiskCache(String key) {
    	if (mDiskCache != null) {
        	try {
        		mDiskCache.remove(hashKeyForDisk(key));
        	} catch (final IOException ioe) {
                Log.e(TAG, "Failed to remove file from disk cache.", ioe);
            }
        	flush();
        }
    }
    
    /**
     * flush() is called to synchronize up other methods that are accessing the
     * cache first
     */
    public void flush() {
    	if (mDiskCache != null) {
        	try {
        		 synchronized (mDiskCache) {
        			if (!mDiskCache.isClosed()) {
        				mDiskCache.flush();
        			}
        		}
        	} catch (final IOException ioe) {
                Log.e(TAG, "Error during disk cache flush.", ioe);
            }
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

    /**
     * Get from memory cache.
     *
     * @param data Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    public Bitmap getBitmapFromMemCache(String data) {
        if (mMemoryCache != null) {
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
        if (data == null) {
            return null;
        }
        
        if (mDiskCache != null) {
        	synchronized (mDiskCache) {
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
        		} catch (final IOException e) {
        			Log.e(TAG, "getBitmapFromDiskCache - ", e);
        		} finally {
        			if (inputStream != null) {
        				try {
        					inputStream.close();
        				} catch (final IOException e) {
            				Log.w(TAG, "Failed to close input stream:", e);
            			}
        			} 
        		}
        	}
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
                        mDiskCache = null;
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "clearCaches - " + e);
                }
                // Clear the memory cache
                mMemoryCache.evictAll();
                return null;
            }
        }.execute((Void[])null);
    }
}
