package za.jamie.soundstage.pablo;

import android.content.Context;
import android.graphics.Bitmap;

import com.squareup.picasso.LruCache;

public class HackCache extends LruCache {

	private static final String PICASSO_THREAD_PREFIX = "Picasso-";
	
	private DiskCache mDiskCache;
	
	public HackCache(Context context) {
		super(context);
		mDiskCache = new DiskCache(context);
	}

	public HackCache(Context context, int maxSize) {
		super(maxSize);
		mDiskCache = new DiskCache(context);
	}
	
	@Override
	public Bitmap get(String key) {
		Bitmap bitmap = super.get(key);
		
		if (bitmap == null && Thread.currentThread().getName().startsWith(PICASSO_THREAD_PREFIX)) {
			bitmap = mDiskCache.get(key);
		}
		
		return bitmap;
	}
	
	@Override
	public void set(String key, Bitmap bitmap) {
		super.set(key, bitmap);
		
		// Save to disk cache
		mDiskCache.set(key, bitmap);
	}
	
	public Bitmap getFromDisk(String key) {
		return mDiskCache.get(key);
	}

}
