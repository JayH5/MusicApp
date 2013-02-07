package com.jamie.play.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;

import com.jamie.play.R;
import com.jamie.play.adapters.AlbumBrowserListAdapter;
import com.jamie.play.adapters.TrackAdapter;
import com.jamie.play.cursormanager.CursorDefinitions;
import com.jamie.play.cursormanager.CursorManager;
import com.jamie.play.fragments.TrackListFragment;
import com.jamie.play.fragments.albumbrowser.AlbumSummaryFragment;

public class AlbumBrowserActivity extends MusicActivity {
	
	//private static final String TAG = "AlbumBrowserActivity";
	private static final String SUMMARY_TAG = "AlbumSummaryFragment";
	private static final String LIST_TAG = "AlbumDetailListFragment";
	
	public static final String EXTRA_ALBUM = "extra_album";
	public static final String EXTRA_ARTIST = "extra_artist";
	public static final String EXTRA_ALBUM_ID = "extra_album_id";
	
	private Bundle mAlbumBundle = new Bundle();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_browser);
		getMenuDrawer().setContentView(R.layout.activity_browser);

		final Intent launchIntent = getIntent();
		if (launchIntent != null) {
			mAlbumBundle.putAll(launchIntent.getExtras());
		} else {
			mAlbumBundle.putAll(savedInstanceState);
		}
		
		String album = mAlbumBundle.getString(EXTRA_ALBUM);
		String artist = mAlbumBundle.getString(EXTRA_ARTIST);
		long albumId = mAlbumBundle.getLong(EXTRA_ALBUM_ID, -1);
		
		// Set the title in the action bar
		final ActionBar actionBar = getActionBar();
		actionBar.setTitle(album);
		actionBar.setSubtitle(artist);
		
		// Get the fragments for the list of tracks and the summary info
		final FragmentManager fm = getSupportFragmentManager();
		
		AlbumSummaryFragment summaryFragment = new AlbumSummaryFragment();
		summaryFragment.setArguments(mAlbumBundle);
		
		ListFragment trackListFragment = new TrackListFragment();
		
		// Set up the adapter for the list
		TrackAdapter adapter = new AlbumBrowserListAdapter(this, 
				R.layout.list_item_track, null, 0);
		trackListFragment.setListAdapter(adapter);
		
		// Display the fragments
		fm.beginTransaction()
			.add(R.id.summaryFrame, summaryFragment, SUMMARY_TAG)
			.add(R.id.listFrame, trackListFragment, LIST_TAG)
			.commit();
		
		final CursorManager cursorManager = new CursorManager(this, adapter, 
				CursorDefinitions.getAlbumBrowserCursorParams(albumId), summaryFragment);
		
		// Set off the cursor loader
		getSupportLoaderManager().initLoader(0, null, cursorManager);
		
	}
		
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(mAlbumBundle);
	}
}
