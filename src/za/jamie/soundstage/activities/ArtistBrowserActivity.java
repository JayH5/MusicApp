package za.jamie.soundstage.activities;

import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.artistbrowser.ArtistAlbumListFragment;
import za.jamie.soundstage.fragments.artistbrowser.ArtistSummaryFragment;
import za.jamie.soundstage.fragments.artistbrowser.ArtistTrackListFragment;
import za.jamie.soundstage.utils.AppUtils;
import za.jay.IcsViewPager.FragmentPagerAdapter;
import za.jay.IcsViewPager.ViewPager;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import com.viewpagerindicator.TitlePageIndicator;

public class ArtistBrowserActivity extends MusicActivity implements 
		ArtistSummaryFragment.OnArtistFoundListener, 
		ArtistTrackListFragment.ArtistTrackListListener {
	
	//public static final String EXTRA_ARTIST_ID = "extra_artist_id";
	private static final String EXTRA_ARTIST_URI = "extra_artist_uri";
	
	private static final String TAG_SUMMARY_FRAG = "artist_summary";
	
	private long mArtistId;
	private Uri mUri;
	
	private ViewPager mViewPager;
	
	private ArtistSummaryFragment mSummaryFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMainContentView(R.layout.activity_artist_browser);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
		
		// Set up the view pager and its indicator
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(new SectionsPagerAdapter(
				getFragmentManager()));
		
		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			AppUtils.loadActionBarTabs(actionBar, mViewPager);
		} else {
			TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
	        //indicator.setViewPager(mViewPager);
		}		

		if (savedInstanceState != null) {
			mUri = savedInstanceState.getParcelable(EXTRA_ARTIST_URI);
		} else {
			mUri = getIntent().getData();
		}
		mArtistId = Long.parseLong(mUri.getLastPathSegment());
				
		// Set up the summary fragment
		final FragmentManager fm = getFragmentManager();
		mSummaryFragment = (ArtistSummaryFragment) fm.findFragmentByTag(TAG_SUMMARY_FRAG);
		if (mSummaryFragment == null) {
			mSummaryFragment = ArtistSummaryFragment.newInstance(mUri);
			fm.beginTransaction()
				.add(R.id.summaryFrame, mSummaryFragment, TAG_SUMMARY_FRAG)
				.commit();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		//outState.putLong(EXTRA_ARTIST_ID, mArtistId);
		outState.putParcelable(EXTRA_ARTIST_URI, mUri);
	}
	
	@Override
	public boolean navigateUpTo(Intent upIntent) {
		upIntent.putExtra(LibraryActivity.EXTRA_SECTION, 
        		LibraryActivity.SECTION_ARTISTS);
        upIntent.putExtra(LibraryActivity.EXTRA_ITEM_ID, mArtistId);
		
		return super.navigateUpTo(upIntent);
	}
	
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return ArtistAlbumListFragment.newInstance(mArtistId);
			case 1:
				return ArtistTrackListFragment.newInstance(mArtistId);
			}
			return null;
		}

		@Override
		public int getCount() {
			return 2;
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			String title = null;
			switch(position) {
			case 0:
				title = getResources().getString(R.string.title_albums).toUpperCase();
				break;
			case 1:
				title = getResources().getString(R.string.title_songs).toUpperCase();
				break;
			}
			return title;
		}

	}

	@Override
	public void onArtistFound(String artist) {
		getActionBar().setTitle(artist);		
	}

	@Override
	public void onDurationCalculated(long duration) {
		mSummaryFragment.setDuration(duration);		
	}

}
