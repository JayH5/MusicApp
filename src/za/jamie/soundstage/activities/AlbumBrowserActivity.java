package za.jamie.soundstage.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.TrackListFragment;
import za.jamie.soundstage.fragments.albumbrowser.AlbumSummaryFragment;
import za.jamie.soundstage.fragments.albumbrowser.AlbumTrackListFragment;
import za.jamie.soundstage.models.AlbumStatistics;
import za.jamie.soundstage.models.AlbumStatistics.Artist;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

public class AlbumBrowserActivity extends MusicActivity implements 
		AlbumTrackListFragment.AlbumStatisticsCallback {
	
	//private static final String TAG = "AlbumBrowserActivity";
	private static final String TAG_LIST_FRAG = "album_track_list";
	
	private long mAlbumId;
	
	private TrackListFragment mTrackListFragment;
	
	private SortedMap<Artist, Integer> mArtists;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMainContentView(R.layout.activity_album_browser);
		
		getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);

		mAlbumId = Long.parseLong(getIntent().getData().getLastPathSegment());		

		final FragmentManager fm = getFragmentManager();
		mTrackListFragment = (TrackListFragment) fm.findFragmentByTag(TAG_LIST_FRAG);
		if (mTrackListFragment == null) {
			mTrackListFragment = AlbumTrackListFragment.newInstance(mAlbumId);			
			fm.beginTransaction()
					.add(R.id.list_frame, mTrackListFragment, TAG_LIST_FRAG)
					.commit();
		}		
	}
	
	@Override
	public boolean navigateUpTo(Intent upIntent) {
		upIntent.putExtra(LibraryActivity.EXTRA_SECTION, 
        		LibraryActivity.SECTION_ALBUMS);
        upIntent.putExtra(LibraryActivity.EXTRA_ITEM_ID, mAlbumId);
		
		return super.navigateUpTo(upIntent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			getMenuInflater().inflate(R.menu.album_browser, menu);
			return true;
		} else {
			return super.onCreateOptionsMenu(menu);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_browse_artist:
			browseArtist();
			return true;
		case R.id.menu_shuffle:
			shuffleAlbum();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void deliverAlbumStatistics(AlbumStatistics album) {
		if (album == null) {
			return;
		}
		
		// Set the title and subtitle in the actionbar
		final ActionBar actionBar = getActionBar();
		actionBar.setTitle(album.title);
		String subtitle = album.isCompilation() ? getString(R.string.various_artists) : 
				album.artists.firstKey().getName();
		actionBar.setSubtitle(subtitle);
		
		mArtists = album.artists;
		
		final FragmentManager fm = getFragmentManager();
		AlbumSummaryFragment frag =
				(AlbumSummaryFragment) fm.findFragmentById(R.id.summary_fragment);
		if (frag != null) {
			frag.loadSummary(album);
		}
	}
	
	public void browseArtist() {
		if (mArtists != null) {
			if (mArtists.size() == 1) {
				launchArtistBrowser(mArtists.firstKey().getId());
			} else {
				buildArtistListDialog(new ArrayList<Artist>(mArtists.keySet())).show();
			}
		}
	}
	
	public void shuffleAlbum() {
		if (mTrackListFragment != null) {
			mTrackListFragment.shuffleAll();
		}
	}
	
	private void launchArtistBrowser(long artistId) {
		Uri data = ContentUris.withAppendedId(
				MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, artistId);
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(data, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE);
		
		startActivity(intent);
	}
	
	private AlertDialog buildArtistListDialog(List<Artist> artists) {
		final ListAdapter adapter = new ArrayAdapter<Artist>(this, 
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
}
