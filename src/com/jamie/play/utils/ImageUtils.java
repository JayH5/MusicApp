package com.jamie.play.utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.jamie.play.bitmapfun.ImageCache;
import com.jamie.play.bitmapfun.ImageFetcher;

public class ImageUtils {
	
	public static final String ALBUM_CACHE_DIR = "album_thumbs";
	public static final String ARTIST_CACHE_DIR = "artist_thumbs";
	
	public static ImageFetcher getImageFetcher(Activity activity) {
		final ImageFetcher fetcher = new ImageFetcher(activity);
		fetcher.setImageCache(ImageCache
				.findOrCreateCache(activity));
		return fetcher;
	}
	
	public static ImageFetcher getImageFetcher(Service service) { 
		final ImageFetcher fetcher = 
				new ImageFetcher(service);
		fetcher.setImageCache(new ImageCache(service));
		return fetcher;
	}
	
	public static DisplayMetrics getDisplayMetrics(final Context context) {
		final DisplayMetrics displayMetrics = new DisplayMetrics();
     	WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
     	wm.getDefaultDisplay().getMetrics(displayMetrics);
     	return displayMetrics;
	}
}
