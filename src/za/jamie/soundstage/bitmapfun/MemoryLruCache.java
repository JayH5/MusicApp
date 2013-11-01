package za.jamie.soundstage.bitmapfun;

import za.jamie.soundstage.utils.AppUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

public class MemoryLruCache implements Cache {

	private LruCache<String, Bitmap> mCache;
	
	public MemoryLruCache(Context context, float memDivider) {
		init(context, AppUtils.calculateMemCacheSize(context, memDivider));
	}
	
	public MemoryLruCache(Context context, int memSize) {
		init(context, memSize);
	}
	
	private void init(Context context, int memSize) {
		mCache = new LruCache<String, Bitmap>(memSize) {
			
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getByteCount();
			}
		};
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