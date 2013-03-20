package za.jamie.soundstage.activities;

import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.artistbrowser.ArtistAlbumListFragment;
import za.jamie.soundstage.fragments.artistbrowser.ArtistSummaryFragment;
import za.jamie.soundstage.fragments.artistbrowser.ArtistTrackListFragment;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.TitlePageIndicator;

public class ArtistBrowserActivity extends MusicActivity {
	
	public static final String EXTRA_ARTIST = "extra_artist";
	public static final String EXTRA_ARTIST_ID = "extra_artist_id";
	
	private static final String TAG_SUMMARY_FRAG = "artist_summary";
	
	private long mArtistId;
	private String mArtist;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMainContentView(R.layout.activity_artist_browser);
		
		// Set up the view pager and its indicator
		ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(new SectionsPagerAdapter(
				getSupportFragmentManager()));
		
		TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);
        		
		// Get the album and artist names from the intent
		final Intent launchIntent = getIntent();
		if (launchIntent != null) {
			mArtist = launchIntent.getStringExtra(EXTRA_ARTIST);
			mArtistId = launchIntent.getLongExtra(EXTRA_ARTIST_ID, -1);
		} else {
			mArtist = savedInstanceState.getString(EXTRA_ARTIST);
			mArtistId = savedInstanceState.getLong(EXTRA_ARTIST_ID, -1);
		}
		
		// Set the title in the action bar
		final ActionBar actionBar = getActionBar();
		actionBar.setTitle(mArtist);
				
		// Set up the summary fragment
		final FragmentManager fm = getSupportFragmentManager();
		if (fm.findFragmentByTag(TAG_SUMMARY_FRAG) == null) {
			fm.beginTransaction()
				.add(R.id.summaryFrame, ArtistSummaryFragment.newInstance(mArtistId),
						TAG_SUMMARY_FRAG)
				.commit();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString(EXTRA_ARTIST, mArtist);
		outState.putLong(EXTRA_ARTIST_ID, mArtistId);
	}
	
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return ArtistAlbumListFragment.newInstance(mArtistId, mArtist);
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

}
