package za.jamie.soundstage.utils;

import za.jamie.soundstage.bitmapfun.ImageCache;
import za.jamie.soundstage.bitmapfun.ImageFetcher;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;


public class ImageUtils {
	
	public static ImageFetcher getImageFetcher(Context context) {
		final ImageFetcher fetcher = new ImageFetcher(context);
		fetcher.setImageCache(ImageCache.getInstance(context));
		return fetcher;
	}
	
	public static DisplayMetrics getDisplayMetrics(final Context context) {
		final DisplayMetrics displayMetrics = new DisplayMetrics();
     	WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
     	wm.getDefaultDisplay().getMetrics(displayMetrics);
     	return displayMetrics;
	}
}
