package com.jamie.play.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.jamie.play.bitmapfun.ImageCache;
import com.jamie.play.bitmapfun.ImageFetcher;

public class ImageUtils {
	
	public static final String DEFAULT_CACHE_DIR = "ImageCache";
	
	/*public static ImageFetcher getImageFetcher(Activity activity) {
		final ImageFetcher fetcher = new ImageFetcher(activity);
		fetcher.setImageCache(ImageCache
				.findOrCreateCache(activity, DEFAULT_CACHE_DIR));
		return fetcher;
	}*/
	
	public static ImageFetcher getImageFetcher(Context context) {
		final ImageFetcher fetcher = new ImageFetcher(context);
		fetcher.setImageCache(ImageCache
				.getInstance(context, DEFAULT_CACHE_DIR));
		return fetcher;
	}
	
	public static ImageCache getImageCache(Context context) {
		return ImageCache.getInstance(context, DEFAULT_CACHE_DIR);
	}
	
	/*public static ImageFetcher getImageFetcher(Service service) { 
		final ImageFetcher fetcher = 
				new ImageFetcher(service);
		fetcher.setImageCache(new ImageCache(service));
		return fetcher;
	}*/
	
	public static DisplayMetrics getDisplayMetrics(final Context context) {
		final DisplayMetrics displayMetrics = new DisplayMetrics();
     	WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
     	wm.getDefaultDisplay().getMetrics(displayMetrics);
     	return displayMetrics;
	}
}
