package za.jamie.soundstage.fragments;

import java.util.ArrayList;
import java.util.List;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.MusicActivity;
import za.jamie.soundstage.models.AlbumStatistics;
import za.jamie.soundstage.models.AlbumStatistics.Artist;
import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.pablo.LastfmUris;
import za.jamie.soundstage.pablo.Pablo;
import za.jamie.soundstage.service.MusicService;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MoreDialogFragment extends MusicDialogFragment implements LoaderCallbacks<Cursor>,
		AdapterView.OnItemClickListener {

	private static final String EXTRA_MUSIC_ITEM = "extra_music_item";
	
	private static final String[] PROJECTION_TRACK = new String[] {
		MediaStore.Audio.Media.ARTIST_ID,
		MediaStore.Audio.Media.ARTIST,
		MediaStore.Audio.Media.ALBUM_ID,
		MediaStore.Audio.Media.ALBUM
	};
	
	private static final String[] PROJECTION_ALBUM = new String[] {
		MediaStore.Audio.Media.ALBUM_ID,
		MediaStore.Audio.Media.ARTIST_ID,
		MediaStore.Audio.Media.ARTIST,
		MediaStore.Audio.Media.ARTIST_KEY
	};
	
	private static final String SELECTION_ALBUM = MediaStore.Audio.Media.ALBUM_ID + "=?";
	
	private MusicItem mItem;
	
	private TextView mSubtitle;
	private ImageView mImage;
	
	private ArrayAdapter<MenuEntry> mMenuAdapter;
	
	public static MoreDialogFragment newInstance(MusicItem item) {
		Bundle args = new Bundle();
		args.putParcelable(EXTRA_MUSIC_ITEM, item);
		
		MoreDialogFragment frag = new MoreDialogFragment();
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mItem = getArguments().getParcelable(EXTRA_MUSIC_ITEM);
		
		mMenuAdapter =
				new ArrayAdapter<MenuEntry>(getActivity(), R.layout.list_item_one_line_dialog, R.id.title);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (mItem.type != MusicItem.TYPE_ARTIST) {
			getLoaderManager().initLoader(0, null, this);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_more_dialog, container, false);
		
		TextView title = (TextView) v.findViewById(R.id.title);
		title.setText(mItem.title);
		mSubtitle = (TextView) v.findViewById(R.id.subtitle);
		mImage = (ImageView) v.findViewById(R.id.image);
		
		ListView lv = (ListView) v.findViewById(R.id.list);
		lv.setAdapter(mMenuAdapter);
		lv.setOnItemClickListener(this);
		
		if (mItem.type == MusicItem.TYPE_ARTIST) {
			loadArtist();
		}
		
		return v;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mMenuAdapter.getItem(position).onClick();
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final MusicItem item = mItem;
		Uri uri = null;
		String[] projection = null;
		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = null;
		switch (item.type) {
		case MusicItem.TYPE_TRACK:
			uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mItem.id);
			projection = PROJECTION_TRACK;
			break;
		case MusicItem.TYPE_ALBUM:
			uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			projection = PROJECTION_ALBUM;
			selection = SELECTION_ALBUM;
			selectionArgs = new String[] { String.valueOf(item.id) };
			sortOrder = MediaStore.Audio.Media.ARTIST_KEY;
			break;
		default:
			return null;
		}
		return new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data == null) {
			return;
		}
		
		switch(mItem.type) {
		case MusicItem.TYPE_TRACK:
			loadTrack(data);
			break;
		case MusicItem.TYPE_ALBUM:
			loadAlbum(data);	
			break;
		default:
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Nothing to do...		
	}
	
	private void loadTrack(Cursor cursor) {
		if (cursor.moveToFirst()) {
			// Get info from cursor
			final long artistId =
					cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
			final String artist =
					cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
			final long albumId =
					cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
			final String album =
					cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
			
			// Set the subtitle
			mSubtitle.setText(artist);
			
			// Load image
			Uri uri = LastfmUris.getAlbumInfoUri(album, artist, albumId);
			Pablo.with(getActivity())
				.load(uri)
				.fit()
				.centerCrop()
				.into(mImage);
			
			// Add menu entries
			mMenuAdapter.add(new MenuEntry("Play track") {
				@Override
				public void onClick() {
					playItem();
				}
			});
			mMenuAdapter.add(new MenuEntry("Add to queue") {
				@Override
				public void onClick() {
					addItemToQueue();
				}
			});
			mMenuAdapter.add(new MenuEntry("Add to playlist") {
				@Override
				public void onClick() {
					addItemToPlayList();
				}
			});
			mMenuAdapter.add(new MenuEntry("Browse album") {
				@Override
				public void onClick() {
					launchAlbumBrowser(albumId);
				}
			});			
			mMenuAdapter.add(new MenuEntry("Browse artist") {
				@Override
				public void onClick() {
					launchArtistBrowser(artistId);
				}
			});			
			mMenuAdapter.add(new MenuEntry("Delete track") {
				@Override
				public void onClick() {
					showDeleteDialog();
				}
			});
		}
	}
	
	private void loadAlbum(Cursor cursor) {
		if (cursor.moveToFirst()) {
			// Get the column indexes in the cursor of the data we need
			final int artistIdColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID);
			final int artistColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
			final int artistKeyColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_KEY);
			
			// Collect the set of artists for the album
			final List<AlbumStatistics.Artist> artists = new ArrayList<AlbumStatistics.Artist>();
			long lastArtistId = 0;			
			do {
				final long artistId = cursor.getLong(artistIdColIdx);
				if (artistId != lastArtistId) {
					artists.add(new AlbumStatistics.Artist(
							cursor.getString(artistKeyColIdx), 
							artistId,
							cursor.getString(artistColIdx)));
					lastArtistId = artistId;
				}
			} while (cursor.moveToNext());
			
			// Set the subtitle
			if (artists.size() == 1) {
				mSubtitle.setText(artists.get(0).getName());
			} else if (artists.size() > 1) {
				mSubtitle.setText(R.string.various_artists);
			}
			
			// Load the image for the album
			Uri uri = LastfmUris.getAlbumInfoUri(mItem.title, artists.get(0).getName(), mItem.id);
			Pablo.with(getActivity())
				.load(uri)
				.fit()
				.centerCrop()
				.into(mImage);
			
			// Add the option to the menu...
			mMenuAdapter.add(new MenuEntry("Play album") {
				@Override
				public void onClick() {
					playItem();
				}
			});
			mMenuAdapter.add(new MenuEntry("Add to queue") {
				@Override
				public void onClick() {
					addItemToQueue();
				}
			});
			mMenuAdapter.add(new MenuEntry("Add to playlist") {
				@Override
				public void onClick() {
					addItemToPlayList();
				}
			});
			mMenuAdapter.add(new MenuEntry(artists.size() > 1 ? "Browse artists" : "Browse artist") {
				@Override
				public void onClick() {
					if (artists.size() == 1) {
						launchArtistBrowser(artists.get(0).getId());
					} else {
						buildArtistListDialog(artists).show();
					}
				}
			});
			mMenuAdapter.add(new MenuEntry("Delete album") {
				@Override
				public void onClick() {
					deleteItem();
				}
			});
		}
	}	
	
	private void loadArtist() {
		// Load the image
		Uri uri = LastfmUris.getArtistInfoUri(mItem.title);
		Pablo.with(getActivity())
			.load(uri)
			.fit()
			.centerCrop()
			.into(mImage);
		
		// Add menu entries
		mMenuAdapter.add(new MenuEntry("Play all songs by artist") {
			@Override
			public void onClick() {
				playItem();
			}
		});
		mMenuAdapter.add(new MenuEntry("Add to queue") {
			@Override
			public void onClick() {
				
			}
		});
		mMenuAdapter.add(new MenuEntry("Add to playlist") {
			@Override
			public void onClick() {
				
			}
		});
		mMenuAdapter.add(new MenuEntry("View lastfm page") {
			@Override
			public void onClick() {
				// TODO
			}
		});
	}
	
	private void playItem() {
		final MusicActivity activity = (MusicActivity) getActivity();
		activity.getMusicConnection().enqueue(mItem, MusicService.PLAY);
		getDialog().dismiss();
		activity.showPlayer();
	}
	
	private AlertDialog buildArtistListDialog(List<Artist> artists) {
		final ListAdapter adapter = new ArrayAdapter<Artist>(getActivity(), 
				R.layout.list_item_one_line_basic, R.id.title, artists) {		
			@Override
			public long getItemId(int position) {
				return getItem(position).getId();
			}
		};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				launchArtistBrowser(adapter.getItemId(which));
			}
		});
		
		builder.setTitle("Browse artists");
		return builder.create();
	}
	
	private void launchArtistBrowser(long artistId) {
		Uri data = ContentUris.withAppendedId(
				MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, artistId);
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(data, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE);
		
		startActivity(intent);
	}
	
	private void launchAlbumBrowser(long albumId) {
		Uri data = ContentUris.withAppendedId(
				MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId);
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(data, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE);
		
		startActivity(intent);
	}
	
	private void addItemToQueue() {
		
	}
	
	private void addItemToPlayList() {
		
	}	
	
	private void showDeleteDialog() {
		String title = null;
		String message = null;
		switch (mItem.type) {
		case MusicItem.TYPE_TRACK:
			title = "Delete track";
			message = "Are you sure you want to delete the track '" + mItem.title + "' from your music collection?";
			break;
		case MusicItem.TYPE_ALBUM:
			title = "Delete album";
			message = "Are you sure you want to delete the album '" + mItem.title + "' from your music collection?";
			break;
		case MusicItem.TYPE_ARTIST:
			title = "Delete artist";
			message = "Are you sure you want to delete all tracks by the artist '" + mItem.title + "' from your music collection?";
			break;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(title)
			.setMessage(message)
			.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					deleteItem();					
				}
			})
			.show();
	}
	
	private void deleteItem() {
		
	}
	
	private class MenuEntry {
		final String mTitle; 
		
		MenuEntry(String title) {
			mTitle = title;
		}
		
		void onClick() { }
		
		@Override
		public String toString() {
			return mTitle;
		}
	}

}
