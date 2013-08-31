package za.jamie.soundstage.activities;

import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.playlistbrowser.PlaylistTrackListFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

public class PlaylistBrowserActivity extends MusicActivity {
	public static final String EXTRA_NAME = "extra_name";
	public static final String EXTRA_PLAYLIST_ID = "extra_playlist_id";
	
	private static final String TAG_LIST_FRAGMENT = "playlist_track_list";
	
	private long mPlaylistId;
	private String mName;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMainContentView(R.layout.activity_album_browser);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		final Intent intent = getIntent();
		mPlaylistId = intent.getLongExtra(EXTRA_PLAYLIST_ID, -1);
		mName = intent.getStringExtra(EXTRA_NAME);
		
		getActionBar().setTitle(mName);
		
		final FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(TAG_LIST_FRAGMENT) == null) {
			fm.beginTransaction()
				.add(R.id.list_frame, PlaylistTrackListFragment.newInstance(mPlaylistId), 
						TAG_LIST_FRAGMENT)
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
            		LibraryActivity.SECTION_PLAYLISTS);
            parentActivityIntent.putExtra(LibraryActivity.EXTRA_ITEM_ID, mPlaylistId);
            startActivity(parentActivityIntent);
            finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
