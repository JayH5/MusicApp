package za.jamie.soundstage.activities;

import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.library.AlbumsFragment;
import za.jamie.soundstage.fragments.library.ArtistsFragment;
import za.jamie.soundstage.fragments.library.PlaylistsFragment;
import za.jamie.soundstage.fragments.library.SongsFragment;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.viewpagerindicator.TitlePageIndicator;

public class LibraryActivity extends MusicActivity {

	// Extras used to recreate activity state when using home-as-up
	public static final String EXTRA_SECTION = "extra_section";
	public static final String EXTRA_ITEM_ID = "extra_item_id";
	
	public static final int SECTION_ARTISTS = 0;
	public static final int SECTION_ALBUMS = 1;
	public static final int SECTION_SONGS = 2;
	public static final int SECTION_PLAYLISTS = 3;
	
	private ViewPager mViewPager;
	
	private int mSelectedPage;
	
	private static final String STATE_SELECTED_PAGE = "selected_page";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMainContentView(R.layout.activity_library);
		
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
		
		// Get the indicator for the view pager
		final TitlePageIndicator indicator = (TitlePageIndicator) 
				findViewById(R.id.indicator);
        indicator.setViewPager(mViewPager);
        Log.d("Lib", "VPI height: " + indicator.getHeight());
        
        if (savedInstanceState != null) {
			mSelectedPage = savedInstanceState.getInt(STATE_SELECTED_PAGE);
		} else {
			mSelectedPage = getIntent().getIntExtra(EXTRA_SECTION, SECTION_ALBUMS);
		}
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
