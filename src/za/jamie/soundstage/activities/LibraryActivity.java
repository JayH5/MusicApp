package za.jamie.soundstage.activities;

import net.simonvt.menudrawer.MenuDrawer;
import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.library.AlbumsFragment;
import za.jamie.soundstage.fragments.library.ArtistsFragment;
import za.jamie.soundstage.fragments.library.PlaylistsFragment;
import za.jamie.soundstage.fragments.library.SongsFragment;
import za.jamie.soundstage.utils.AppUtils;
import za.jamie.soundstage.widgets.PagerSlidingTabStrip;
import za.jay.IcsViewPager.FragmentPagerAdapter;
import za.jay.IcsViewPager.ViewPager;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

public class LibraryActivity extends MusicActivity {

	// Extras used to recreate activity state when using home-as-up
	protected static final String EXTRA_SECTION = "extra_section";
	protected static final String EXTRA_ITEM_ID = "extra_item_id";
	
	private static final String STATE_SELECTED_PAGE = "selected_page";
	
	public static final int SECTION_ARTISTS = 0;
	public static final int SECTION_ALBUMS = 1;
	public static final int SECTION_SONGS = 2;
	public static final int SECTION_PLAYLISTS = 3;
	
	private ViewPager mViewPager;
	private MenuDrawer mDrawer;
	
	private int mSelectedPage;	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMainContentView(R.layout.activity_library);
		
		// Enable drawer animated icon
		mDrawer = getDrawer();
		mDrawer.setSlideDrawable(R.drawable.ic_drawer);
		mDrawer.setDrawerIndicatorEnabled(true);
		
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(new SectionsPagerAdapter(getFragmentManager()));
		
		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayShowTitleEnabled(false);
			AppUtils.loadActionBarTabs(getActionBar(), mViewPager);
		} else {
			// Get the indicator for the view pager
			final PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip)
					findViewById(R.id.indicator);
			tabStrip.setViewPager(mViewPager);
		}		
        
        if (savedInstanceState != null) {
			mSelectedPage = savedInstanceState.getInt(STATE_SELECTED_PAGE);
		} else {
			Log.d("Library", "Incoming intent: " + getIntent());
			mSelectedPage = getIntent().getIntExtra(EXTRA_SECTION, SECTION_ALBUMS);
		}
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.toggleMenu();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mViewPager.setCurrentItem(mSelectedPage);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_SELECTED_PAGE, mViewPager.getCurrentItem());
	}
	
	@Override
	public void onBackPressed() {
		final int drawerState = mDrawer.getDrawerState();
		if (isPlaying() && drawerState != MenuDrawer.STATE_OPEN && 
				drawerState != MenuDrawer.STATE_OPENING) {
			showNotification();
		}
		super.onBackPressed();
	}
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			long itemId = -1;
			if (position == mSelectedPage) {
				itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
			}
			switch (position) {
			case SECTION_ARTISTS:
				return ArtistsFragment.newInstance(itemId);
			case SECTION_ALBUMS:
				return AlbumsFragment.newInstance(itemId);
			case SECTION_SONGS:
				return new SongsFragment();
			case SECTION_PLAYLISTS:
				return PlaylistsFragment.newInstance(itemId);
			}
			return null;
		}

		@Override
		public int getCount() {
			// Show 4 total pages.
			return 4;
		}

		@SuppressLint("DefaultLocale")
		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case SECTION_ARTISTS:
				return getString(R.string.title_artists).toUpperCase();
			case SECTION_ALBUMS:
				return getString(R.string.title_albums).toUpperCase();
			case SECTION_SONGS:
				return getString(R.string.title_songs).toUpperCase();
			case SECTION_PLAYLISTS:
				return getString(R.string.title_playlists).toUpperCase();
			}
			return null;
		}
	}
}
