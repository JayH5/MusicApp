package za.jamie.soundstage.activities;

import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.albumbrowser.AlbumSummaryFragment;
import za.jamie.soundstage.fragments.albumbrowser.AlbumTrackListFragment;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

public class AlbumBrowserActivity extends MusicActivity {
	
	//private static final String TAG = "AlbumBrowserActivity";
	private static final String TAG_LIST_FRAG = "album_track_list";
	private static final String TAG_SUMMARY_FRAG = "album_summary";
	
	public static final String EXTRA_ALBUM = "extra_album";
	public static final String EXTRA_ARTIST = "extra_artist";
	public static final String EXTRA_ALBUM_ID = "extra_album_id";
	
	private String mAlbum;
	private String mArtist;
	private long mAlbumId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMainContentView(R.layout.activity_browser);

		final Intent launchIntent = getIntent();
		if (launchIntent != null) {
			mAlbum = launchIntent.getStringExtra(EXTRA_ALBUM);
			mArtist = launchIntent.getStringExtra(EXTRA_ARTIST);
			mAlbumId = launchIntent.getLongExtra(EXTRA_ALBUM_ID, -1);
		} else {
			mAlbum = savedInstanceState.getString(EXTRA_ALBUM);
			mArtist = savedInstanceState.getString(EXTRA_ARTIST);
			mAlbumId = savedInstanceState.getLong(EXTRA_ALBUM_ID, -1);
		}
		
		// Set the title in the action bar
		final ActionBar actionBar = getActionBar();
		actionBar.setTitle(mAlbum);
		actionBar.setSubtitle(mArtist);
		
		// Get the fragments for the list of tracks and the summary info
		final FragmentManager fm = getSupportFragmentManager();
		
		if (fm.findFragmentByTag(TAG_SUMMARY_FRAG) == null) {
			fm.beginTransaction()
				.add(R.id.summaryFrame, AlbumSummaryFragment.newInstance(mAlbumId), 
						TAG_SUMMARY_FRAG)
				.commit();
		}
							
		if (fm.findFragmentByTag(TAG_LIST_FRAG) == null) {
			fm.beginTransaction()
				.add(R.id.listFrame, AlbumTrackListFragment.newInstance(mAlbumId), 
						TAG_LIST_FRAG)
				.commit();
		}	
	}
		
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(EXTRA_ALBUM_ID, mAlbumId);
		outState.putString(EXTRA_ALBUM, mAlbum);
		outState.putString(EXTRA_ARTIST, mArtist);
	}
}
