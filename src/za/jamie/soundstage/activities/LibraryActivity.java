package za.jamie.soundstage.activities;

import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.library.AlbumsFragment;
import za.jamie.soundstage.fragments.library.ArtistsFragment;
import za.jamie.soundstage.fragments.library.SongsFragment;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.TitlePageIndicator;

public class LibraryActivity extends MusicActivity {

	private static final int SECTION_ALBUMS = 0;
	private static final int SECTION_ARTISTS = 1;
	private static final int SECTION_SONGS = 2;
	
	private ViewPager mViewPager;
	
	private static final String STATE_SELECTED_PAGE = "selected_page";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getMenuDrawer().setContentView(R.layout.activity_library);
		
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
		
		// Get the indicator for the view pager
		TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(mViewPager);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState.containsKey(STATE_SELECTED_PAGE)) {
			mViewPager.setCurrentItem(
					savedInstanceState.getInt(STATE_SELECTED_PAGE));
		}
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
			switch (position) {
			case SECTION_ALBUMS:
				return new AlbumsFragment();
			case SECTION_ARTISTS:
				return new ArtistsFragment();
			case SECTION_SONGS:
				return new SongsFragment();
			}
			return null;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@SuppressLint("DefaultLocale")
		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.title_albums).toUpperCase();
			case 1:
				return getString(R.string.title_artists).toUpperCase();
			case 2:
				return getString(R.string.title_songs).toUpperCase();
			}
			return null;
		}
	}
}
