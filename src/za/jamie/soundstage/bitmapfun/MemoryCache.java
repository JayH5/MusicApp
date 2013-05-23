package za.jamie.soundstage.bitmapfun;

import za.jamie.soundstage.utils.AppUtils;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class MemoryCache implements Cache<String, Bitmap> {

	private LruCache<String, Bitmap> mCache;
	
	public MemoryCache(Context context, float memDivider) {
		init(context, AppUtils.calculateMemCacheSize(context, memDivider));
	}
	
	public MemoryCache(Context context, int memSize) {
		init(context, memSize);
	}
	
	private void init(Context context, int memSize) {
		mCache = new LruCache<String, Bitmap>(memSize) {
			
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getByteCount();
			}
		};
		registerComponentCallbacks2(context);
	}
	
	private void registerComponentCallbacks2(Context context) {
    	context.registerComponentCallbacks(new ComponentCallbacks2() {
            
			@Override
            public void onTrimMemory(final int level) {
                if (level >= TRIM_MEMORY_MODERATE) {
                    mCache.evictAll();
                } else if (level >= TRIM_MEMORY_BACKGROUND) {
                    mCache.trimToSize(mCache.size() / 2);
                }
            }

			@Override
			public void onConfigurationChanged(Configuration newConfig) {}

			@Override
			public void onLowMemory() {}
    	});
    }
	
	@Override
	public synchronized void put(String key,Bitmap value) {
		if (mCache.get(key) == null) {
			mCache.put(key, value);
		}
	}
	
	@Override
	public void remove(String key) {
		mCache.remove(key);
	}
	
	@Override
	public Bitmap get(String key) {
		return mCache.get(key);
	}
	
	@Override
	public void clear() {
		mCache.evictAll();
	}
	
	@Override
	public void close() {
		clear();
	}

}
