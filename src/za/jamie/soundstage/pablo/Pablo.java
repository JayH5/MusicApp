package za.jamie.soundstage.pablo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

public class Pablo {

	private static final float CACHE_PORTION = 0.2f;
	private static final float SERVICE_CACHE_PORTION = 0.05f;
	
	private static Picasso sPicasso;
	private static Picasso sServicePicasso;
	
	public static Picasso with(Context context) {
		if (sPicasso == null) {
			sPicasso = buildPicasso(context.getApplicationContext());
		}
		return sPicasso;
	}
	
	public static Picasso with(Service service) {
		if (sServicePicasso == null) {
			sServicePicasso = buildServicePicasso(service.getApplicationContext());
		}
		return sServicePicasso;
	}
	
	private static Picasso buildPicasso(Context context) {
		Picasso.Builder builder = new Picasso.Builder(context);
		builder.downloader(new LastfmDownloader(context))
			.memoryCache(new LruCache(calculateMemoryCacheSize(context, CACHE_PORTION)))
			.diskCache(new DiskCache(context));
		return builder.build();
	}
	
	private static Picasso buildServicePicasso(Context context) {
		Picasso.Builder builder = new Picasso.Builder(context);
		builder.downloader(new LastfmDownloader(context))
			.executor(getSingleThreadExecutor())
			.memoryCache(new LruCache(calculateMemoryCacheSize(context, SERVICE_CACHE_PORTION)))
			.diskCache(new DiskCache(context));
		return builder.build();
	}
	
	private static ExecutorService getSingleThreadExecutor() {
		return Executors.newSingleThreadExecutor(new PabloThreadFactory());
	}
	
	private static class PabloThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(Runnable r) {
			return new PabloThread(r);
		}
	}
	
	private static class PabloThread extends Thread {
		public PabloThread(Runnable r) {
			super(r);
		}
		
		@Override
		public void run() {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
			super.run();
		}
	}
	
	private static int calculateMemoryCacheSize(Context context, float portion) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		boolean largeHeap = (context.getApplicationInfo().flags & ApplicationInfo.FLAG_LARGE_HEAP) != 0;
		int memoryClass = largeHeap ? am.getLargeMemoryClass() : am.getMemoryClass();
		
		return (int) (1024 * 1024 * memoryClass * portion);
	}

}
