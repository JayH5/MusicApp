package za.jamie.soundstage.activities;

import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.playlistbrowser.PlaylistTrackListFragment;
import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;

public class PlaylistBrowserActivity extends MusicActivity implements LoaderCallbacks<Cursor> {
	
	//private static final String TAG = "PlaylistBrowserActivity";
	
	public static final String EXTRA_NAME = "extra_name";
	public static final String EXTRA_PLAYLIST_ID = "extra_playlist_id";
	
	private static final String TAG_LIST_FRAGMENT = "playlist_track_list";
	
	private long mPlaylistId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_album_browser);
		
		getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
		
		mPlaylistId = Long.parseLong(getIntent().getData().getLastPathSegment());
		
		final FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(TAG_LIST_FRAGMENT) == null) {
			fm.beginTransaction()
				.add(R.id.list_frame, PlaylistTrackListFragment.newInstance(mPlaylistId), 
						TAG_LIST_FRAGMENT)
				.commit();
		}
		
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public boolean navigateUpTo(Intent upIntent) {
		upIntent.putExtra(LibraryActivity.EXTRA_SECTION, LibraryActivity.SECTION_PLAYLISTS);
        upIntent.putExtra(LibraryActivity.EXTRA_ITEM_ID, mPlaylistId);
		
		return super.navigateUpTo(upIntent);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this,
				getIntent().getData(),
				new String[] { MediaStore.Audio.Playlists.NAME },
				null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data != null && data.moveToFirst()) {
			getActionBar().setTitle(
					data.getString(data.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME)));
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Nothing to do...
	}
}
