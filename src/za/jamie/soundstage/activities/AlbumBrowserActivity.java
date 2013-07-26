package za.jamie.soundstage.activities;

import java.util.List;

import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.ImageDialogFragment;
import za.jamie.soundstage.fragments.TrackListFragment;
import za.jamie.soundstage.fragments.albumbrowser.AlbumTrackListFragment;
import za.jamie.soundstage.models.AlbumStatistics;
import za.jamie.soundstage.models.AlbumStatistics.Artist;
import za.jamie.soundstage.utils.ImageUtils;
import za.jamie.soundstage.utils.TextUtils;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.TaskStackBuilder;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class AlbumBrowserActivity extends MusicActivity implements 
		AlbumTrackListFragment.AlbumStatisticsCallback {
	
	//private static final String TAG = "AlbumBrowserActivity";
	private static final String TAG_LIST_FRAG = "album_track_list";
	private static final String TAG_IMAGE_DIALOG = "tag_image_dialog";

	private static final String EXTRA_ALBUM_ID = "extra_album_id";
	
	private long mAlbumId;
	
	private TextView mNumTracksText;
	private TextView mDurationText;
	private TextView mYearsText;
	
	private TrackListFragment mTrackListFragment;
	
	private List<Artist> mArtists;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMainContentView(R.layout.activity_browser);
		
		getActionBar().setDisplayOptions(0, 
				ActionBar.DISPLAY_SHOW_HOME);

		if (savedInstanceState != null) {
			mAlbumId = savedInstanceState.getLong(EXTRA_ALBUM_ID);
		}
		else {
			mAlbumId = Long.parseLong(getIntent().getData().getLastPathSegment());
		}
		
		initViews();		
		initTrackList();		
	}
	
	private void initViews() {
		mNumTracksText = (TextView) findViewById(R.id.albumTracks);
		mDurationText = (TextView) findViewById(R.id.albumDuration);
		mYearsText = (TextView) findViewById(R.id.albumYear);
		
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			final ImageButton browseArtistButton = 
					(ImageButton) findViewById(R.id.browse_artist_button);
			browseArtistButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					browseArtist();					
				}
			});
			
			final ImageButton shuffleButton = 
					(ImageButton) findViewById(R.id.shuffle_button);
			shuffleButton.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					shuffleAlbum();
				}
			});
		}
		
		// Load up the album artwork
		ImageView albumArt = (ImageView) findViewById(R.id.albumThumb);
		ImageUtils.getThumbImageFetcher(this).loadAlbumImage(mAlbumId, albumArt);
		albumArt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ImageDialogFragment.newInstance(String.valueOf(mAlbumId))
						.show(getFragmentManager(), TAG_IMAGE_DIALOG);
				
			}
		});
	}
	
	private void initTrackList() {
		final FragmentManager fm = getFragmentManager();
		mTrackListFragment = (TrackListFragment) fm.findFragmentByTag(TAG_LIST_FRAG);
		if (mTrackListFragment == null) {
			mTrackListFragment = AlbumTrackListFragment.newInstance(mAlbumId);
			
			fm.beginTransaction()
					.add(R.id.listFrame, mTrackListFragment, TAG_LIST_FRAG)
					.commit();
		}
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
	public boolean navigateUpTo(Intent upIntent) {
		upIntent.putExtra(LibraryActivity.EXTRA_SECTION, 
            		LibraryActivity.SECTION_ALBUMS);
		upIntent.putExtra(LibraryActivity.EXTRA_ITEM_ID, mAlbumId);
		
		return super.navigateUpTo(upIntent);
	}
	
	@Override
	public Intent getParentActivityIntent() {
		Log.d("AlbumBrowser", "Getting parent intent.");
		Intent intent = super.getParentActivityIntent();
		intent.putExtra(LibraryActivity.EXTRA_ITEM_ID, mAlbumId);
		return intent;
	}
	
	@Override
	public void onCreateNavigateUpTaskStack(TaskStackBuilder builder) {
		super.onCreateNavigateUpTaskStack(builder);
		
		Log.d("AlbumBrowser", "Creating task stack");
	}
		
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putLong(EXTRA_ALBUM_ID, mAlbumId);
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
				album.artists.get(0).toString();
		actionBar.setSubtitle(subtitle);
		
		// Load all the stats into the textviews
		final Resources res = getResources();
		mNumTracksText.setText(TextUtils.getNumTracksText(res, album.numTracks));
		mDurationText.setText(TextUtils.getStatsDurationText(res, album.duration));
		mYearsText.setText(TextUtils.getYearText(album.firstYear, album.lastYear));
		
		mArtists = album.artists;
	}
	
	private void browseArtist() {
		if (mArtists != null) {
			if (mArtists.size() == 1) {
				launchArtistBrowser(mArtists.get(0).getId());
			} else {
				buildArtistListDialog(mArtists).show();
			}
		}
	}
	
	private void shuffleAlbum() {
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
				R.layout.list_item_one_line, R.id.title, artists) {
			
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
