package za.jamie.soundstage.utils;

import java.io.File;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

public class AppUtils {
	
	public static final boolean isApplicationSentToBackground(final Context context) {
        final ActivityManager activityManager = (ActivityManager)context
                .getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            final ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }
	
	/**
     * Get a usable cache directory (external if available, internal otherwise)
     * 
     * @param context The {@link Context} to use
     * @param uniqueName A unique directory name to append to the cache
     *            directory
     * @return The cache directory
     */
	public static final File getCacheDir(Context context, String uniqueName) {
		final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState()) || !Environment.isExternalStorageRemovable()
                ? context.getExternalCacheDir().getPath() : context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
	}
	
	public static final int calculateMemCacheSize(Context context, float memoryDivider) {
		int memoryClass = ((ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE)).getMemoryClass();
    	return (int) (1024 * 1024 * memoryClass * memoryDivider);
	}
	
	public static final void loadActionBarTabs(final ActionBar actionBar, final ViewPager viewPager) {
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		// Create a simple listener for the tabs
		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) { }
			
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				viewPager.setCurrentItem(tab.getPosition());				
			}
			
			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) { }
		};
		
		// Get all the tab titles from the viewpager's adapter
		PagerAdapter pagerAdapter = viewPager.getAdapter();
		for (int i = 0; i < pagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab()
					.setText(pagerAdapter.getPageTitle(i))
					.setTabListener(tabListener));
		}
		
		// Set a listener for the viewpager to update current tab
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
			
			@Override
			public void onPageScrollStateChanged(int state) { }

			@Override
			public void onPageScrolled(int position, float positionOffset, 
					int positionOffsetPixels) { }
		});
	}
}
