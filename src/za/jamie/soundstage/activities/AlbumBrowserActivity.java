package za.jamie.soundstage.activities;

import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.albumbrowser.AlbumSummaryFragment;
import za.jamie.soundstage.fragments.albumbrowser.AlbumTrackListFragment;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

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
		
		getActionBar().setDisplayHomeAsUpEnabled(true);

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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This is called when the Home (Up) button is pressed
            // in the Action Bar.
            Intent parentActivityIntent = new Intent(this, LibraryActivity.class);
            parentActivityIntent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            parentActivityIntent.putExtra(LibraryActivity.EXTRA_SECTION, 
            		LibraryActivity.SECTION_ALBUMS);
            parentActivityIntent.putExtra(LibraryActivity.EXTRA_ITEM_ID, mAlbumId);
            startActivity(parentActivityIntent);
            finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
		
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(EXTRA_ALBUM_ID, mAlbumId);
		outState.putString(EXTRA_ALBUM, mAlbum);
		outState.putString(EXTRA_ARTIST, mArtist);
	}
}
