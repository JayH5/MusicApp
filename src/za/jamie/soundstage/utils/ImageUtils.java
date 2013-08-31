package za.jamie.soundstage.utils;

import za.jamie.soundstage.R;
import za.jamie.soundstage.bitmapfun.DiskCache;
import za.jamie.soundstage.bitmapfun.ImageFetcher;
import za.jamie.soundstage.bitmapfun.ImageResizer;
import za.jamie.soundstage.bitmapfun.MemoryLruCache;
import za.jamie.soundstage.bitmapfun.SingleBitmapCache;
import android.content.Context;
import android.util.Log;

public class ImageUtils {
	
	private static final String TAG = "ImageUtils";
	
	private static final float MEM_CACHE_DIVIDER_THUMB = 0.3f;
	private static final String DISK_CACHE_DIR_THUMB = "ThumbCache";
	private static final String DISK_CACHE_DIR_BIG = "ImageCache";
	private static final int DISK_CACHE_QUALITY_THUMB = 75;
	
	private static MemoryLruCache sThumbMemoryCache = null;
	private static DiskCache sThumbDiskCache = null;
	
	private static SingleBitmapCache sBigMemoryCache = null;
	private static DiskCache sBigDiskCache = null;
	
	/**
	 * 
	 * @param context
	 * @return The image fetcher for thumbnail images (150 x 150dp)
	 */
	public static ImageFetcher getThumbImageFetcher(Context context) {
		
		final ImageFetcher fetcher = new ImageFetcher(context, getThumbResizer(context));
		fetcher.setMemoryCache(getThumbMemoryCacheInstance(context));
		fetcher.setDiskCache(getThumbDiskCacheInstance(context));
		return fetcher;
	}
	
	public static ImageFetcher getBigImageFetcher(Context context) {
		final ImageFetcher fetcher = new ImageFetcher(context, getBigResizer(context));
		fetcher.setMemoryCache(getBigMemoryCacheInstance());
		fetcher.setDiskCache(getBigDiskCacheInstance(context));
		return fetcher;
	}
	
	public static MemoryLruCache getThumbMemoryCacheInstance(Context context) {
		if (sThumbMemoryCache == null) {
			sThumbMemoryCache = new MemoryLruCache(context, MEM_CACHE_DIVIDER_THUMB);
		}
		return sThumbMemoryCache;
	}
	
	public static synchronized DiskCache getThumbDiskCacheInstance(Context context) {
		if (sThumbDiskCache == null) {
			sThumbDiskCache = new DiskCache(context, DISK_CACHE_DIR_THUMB);
			sThumbDiskCache.getCacheParams().setCompressQuality(DISK_CACHE_QUALITY_THUMB);
		}
		return sThumbDiskCache;
	}
	
	public static SingleBitmapCache getBigMemoryCacheInstance() {
		if (sBigMemoryCache == null) {
			sBigMemoryCache = new SingleBitmapCache();
		}
		return sBigMemoryCache;
	}
	
	public static synchronized DiskCache getBigDiskCacheInstance(Context context) {
		if (sBigDiskCache == null) {
			Log.d(TAG, "Creating big image disk cache instance.");
			sBigDiskCache = new DiskCache(context, DISK_CACHE_DIR_BIG);
		}
		return sBigDiskCache;
	}
	
	public static ImageResizer getThumbResizer(Context context) {
		int dimen = context.getResources().getDimensionPixelSize(R.dimen.image_thumb_album);
		return new ImageResizer(context, dimen);
	}
	
	public static ImageResizer getBigResizer(Context context) {
		int dimen = context.getResources().getDisplayMetrics().widthPixels;
		return new ImageResizer(context, dimen);
	}
}
