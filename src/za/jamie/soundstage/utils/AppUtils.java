package za.jamie.soundstage.utils;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;

import java.io.File;

public final class AppUtils {
	
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

        return new File(cachePath, uniqueName);
	}

    public static File getAppDir(Context context, String uniqueName) {
        String path = !Environment.isExternalStorageRemovable()
                || Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                ? context.getExternalFilesDir(null).getPath() : context.getFilesDir().getPath();

        return new File(path, uniqueName);
    }
	
	public static final void loadActionBarTabs(final ActionBar actionBar,
                                               final ViewPager viewPager) {
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
	
	public static boolean isLandscape(Resources res) {
		return res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}
	
	public static boolean isPortrait(Resources res) {
		return res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}

    public static int smallestScreenWidth(Resources res) {
        DisplayMetrics dm = res.getDisplayMetrics();
        return Math.min(dm.widthPixels, dm.heightPixels);
    }
}
