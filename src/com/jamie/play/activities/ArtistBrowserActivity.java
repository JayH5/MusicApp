package com.jamie.play.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.jamie.play.R;
import com.jamie.play.adapters.ArtistTracksAdapter;
import com.jamie.play.adapters.TrackAdapter;
import com.jamie.play.cursormanager.CursorDefinitions;
import com.jamie.play.cursormanager.CursorManager;
import com.jamie.play.fragments.TrackListFragment;
import com.jamie.play.fragments.artistbrowser.ArtistAlbumsFragment;
import com.jamie.play.fragments.artistbrowser.ArtistSummaryFragment;
import com.viewpagerindicator.TitlePageIndicator;

public class ArtistBrowserActivity extends MusicActivity {
	
	public static final String EXTRA_ARTIST = "extra_artist";
	public static final String EXTRA_ARTIST_ID = "extra_artist_id";
	
	private String mArtist;
	
	private Bundle mArtistBundle = new Bundle();
	
	private TrackListFragment mArtistTracksFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_artist_browser);
		getMenuDrawer().setContentView(R.layout.activity_artist_browser);
		
		// Set up the view pager and its indicator
		ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(new SectionsPagerAdapter(
				getSupportFragmentManager()));
		
		TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);
        		
		// Get the album and artist names from the intent
		final Intent launchIntent = getIntent();
		if (launchIntent != null) {
			mArtistBundle.putAll(launchIntent.getExtras());
		} else {
			mArtistBundle.putAll(savedInstanceState);
		}
		
		mArtist = mArtistBundle.getString(EXTRA_ARTIST);
		long artistId = mArtistBundle.getLong(EXTRA_ARTIST_ID);
		
		// Set the title in the action bar
		final ActionBar actionBar = getActionBar();
		actionBar.setTitle(mArtist);
				
		// Set up the summary of the album
		ArtistSummaryFragment summaryFragment = new ArtistSummaryFragment();
		summaryFragment.setArguments(mArtistBundle);
		
		// Set up the tracks list fragment and its adapter
		TrackAdapter adapter = new ArtistTracksAdapter(this, 
				R.layout.list_item_two_line, null, 0);
		
		mArtistTracksFragment = new TrackListFragment();
		mArtistTracksFragment.setListAdapter(adapter);
		
		// Set up the cursor manager to load cursor for summary and track list
		final CursorManager cm = new CursorManager(this, 
				adapter, CursorDefinitions.getArtistBrowserCursorParams(artistId), summaryFragment);
		
		// Display the summary fragment
		final FragmentManager fm = getSupportFragmentManager();
		fm.beginTransaction()
				.add(R.id.summaryFrame, summaryFragment)
				.commit();
		
		getSupportLoaderManager().initLoader(0, null, cm);
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(mArtistBundle);
	}
	
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return ArtistAlbumsFragment.newInstance(mArtist);
			case 1:
				return mArtistTracksFragment;
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
