package za.jamie.soundstage.activities;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.playlistbrowser.PlaylistSummaryFragment;
import za.jamie.soundstage.fragments.playlistbrowser.PlaylistTrackListFragment;
import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.models.PlaylistStatistics;

public class PlaylistBrowserActivity extends MusicActivity implements LoaderCallbacks<Cursor>,
        PlaylistTrackListFragment.PlaylistTrackListHost,
        PlaylistSummaryFragment.PlaylistSummaryHost {

	//private static final String TAG = "PlaylistBrowserActivity";

    private static final String STATE_PLAYLIST_ID = "state_playlist_id";

	private static final String TAG_LIST_FRAGMENT = "playlist_track_list";

	private long mPlaylistId;
    private String mPlaylist;

    private PlaylistTrackListFragment mTrackListFragment;

    private SortedSet<MusicItem> mArtists;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_browser);

		getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);

		if (savedInstanceState != null) {
            mPlaylistId = savedInstanceState.getLong(STATE_PLAYLIST_ID);
        } else {
            mPlaylistId = ContentUris.parseId(getIntent().getData());
        }

        final FragmentManager fm = getFragmentManager();
        mTrackListFragment = (PlaylistTrackListFragment) fm.findFragmentByTag(TAG_LIST_FRAGMENT);
		if (mTrackListFragment == null) {
            mTrackListFragment = PlaylistTrackListFragment.newInstance(mPlaylistId);
			fm.beginTransaction()
				.add(R.id.list_frame, mTrackListFragment, TAG_LIST_FRAGMENT)
				.commit();
		}

		getLoaderManager().initLoader(0, null, this);
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_PLAYLIST_ID, mPlaylistId);
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
            mPlaylist = data.getString(data.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME));
			getActionBar().setTitle(mPlaylist);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Nothing to do...
	}

    @Override
    public void browseArtists() {
        if (mArtists != null) {
            if (mArtists.size() == 1) {
                launchArtistBrowser(mArtists.first().getId());
            } else {
                buildArtistListDialog(new ArrayList<MusicItem>(mArtists));
            }
        }
    }

    private void launchArtistBrowser(long artistId) {
        Uri data = ContentUris.withAppendedId(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, artistId);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(data, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE);

        startActivity(intent);
    }

    private AlertDialog buildArtistListDialog(List<MusicItem> artists) {
        final ListAdapter adapter = new ArrayAdapter<MusicItem>(this,
                R.layout.list_item_one_line_basic, R.id.title, artists) {
            @Override
            public long getItemId(int position) {
                return getItem(position).getId();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                launchArtistBrowser(adapter.getItemId(which));
            }
        });

        builder.setTitle("Browse artists");
        return builder.create();
    }

    @Override
    public void deliverPlaylistStatistics(PlaylistStatistics stats) {
        if (stats == null) {
            return;
        }

        mArtists = stats.artists;
        final FragmentManager fm = getFragmentManager();
        PlaylistSummaryFragment frag = (PlaylistSummaryFragment)
                fm.findFragmentById(R.id.summary_fragment);
        if (frag != null) {
            frag.loadSummary(stats);
        }
    }

    @Override
    public void playAt(int position) {
        getMusicConnection()
                .open(new MusicItem(mPlaylistId, mPlaylist, MusicItem.TYPE_PLAYLIST), position);
        showPlayer();
    }

    @Override
    public void shuffleAll() {
        getMusicConnection()
                .shuffle(new MusicItem(mPlaylistId, mPlaylist, MusicItem.TYPE_PLAYLIST));
        showPlayer();
    }
}
