package za.jamie.soundstage.activities;

import java.util.List;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.AlbumTrackListAdapter;
import za.jamie.soundstage.fragments.ImageDialogFragment;
import za.jamie.soundstage.fragments.TrackListFragment;
import za.jamie.soundstage.fragments.albumbrowser.AlbumTrackListFragment;
import za.jamie.soundstage.models.AlbumStatistics;
import za.jamie.soundstage.models.Artist;
import za.jamie.soundstage.utils.ImageUtils;
import za.jamie.soundstage.utils.TextUtils;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class AlbumBrowserActivity extends MusicActivity implements 
		AlbumTrackListAdapter.StatsCallback {
	
	//private static final String TAG = "AlbumBrowserActivity";
	private static final String TAG_LIST_FRAG = "album_track_list";
	private static final String TAG_IMAGE_DIALOG = "tag_image_dialog";

	private static final String EXTRA_ALBUM_ID = "extra_album_id";
	
	private long mAlbumId;
	
	private TextView mNumTracksText;
	private TextView mDurationText;
	private TextView mYearsText;
	private ImageButton mBrowseArtistButton;
	
	private TrackListFragment mTrackListFragment;
	
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
		
		mBrowseArtistButton = 
				(ImageButton) findViewById(R.id.browse_artist_button);
		
		final ImageButton shuffleButton = 
				(ImageButton) findViewById(R.id.shuffle_button);
		shuffleButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mTrackListFragment.shuffleAll();
				
			}
		});
		
		// Load up the album artwork
		ImageView albumArt = (ImageView) findViewById(R.id.albumThumb);
		ImageUtils.getThumbImageFetcher(this).loadAlbumImage(mAlbumId, albumArt);
		albumArt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ImageDialogFragment.newInstance(String.valueOf(mAlbumId))
						.show(getSupportFragmentManager(), TAG_IMAGE_DIALOG);
				
			}
		});
	}
	
	private void initTrackList() {
		final FragmentManager fm = getSupportFragmentManager();
		mTrackListFragment = (TrackListFragment) fm.findFragmentByTag(TAG_LIST_FRAG);
		if (mTrackListFragment == null) {
			mTrackListFragment = AlbumTrackListFragment.newInstance(mAlbumId);
			
			fm.beginTransaction()
					.add(R.id.listFrame, mTrackListFragment, TAG_LIST_FRAG)
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
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putLong(EXTRA_ALBUM_ID, mAlbumId);
	}

	@Override
	public void onStatisticsCalculated(AlbumStatistics album) {
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
		mYearsText.setText(TextUtils.getYearText(res, album.firstYear, album.lastYear));
		
		// Set up the browse artists button
		initBrowseArtistButton(album.artists);
	}
	
	/**
	 * Set the browse artists button to either go to the album's artist (if there
	 * is only one artist) or display a list of all the artists on the album.
	 * @param artists The list of artists featured on the album
	 */
	private void initBrowseArtistButton(final List<Artist> artists) {
		if (artists.size() == 1) {
			mBrowseArtistButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					launchArtistBrowser(artists.get(0).getId());
					
				}
			});
		} else {
			mBrowseArtistButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					buildArtistListDialog(artists).show();
					
				}
			});
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
