package za.jamie.soundstage.fragments;

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

import java.util.ArrayList;
import java.util.List;

import za.jamie.soundstage.R;
import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.pablo.DeferredWidthRequestCreator;
import za.jamie.soundstage.pablo.Pablo;
import za.jamie.soundstage.pablo.SoundstageUris;
import za.jamie.soundstage.service.MusicService;
import za.jamie.soundstage.utils.AppUtils;

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
		MediaStore.Audio.Media.ARTIST_ID,
		MediaStore.Audio.Media.ARTIST
	};
	
	private static final String[] PROJECTION_PLAYLIST = new String[] {
		MediaStore.Audio.Media.ALBUM_ID,
		MediaStore.Audio.Media.ALBUM,
		MediaStore.Audio.Media.ARTIST_ID,
		MediaStore.Audio.Media.ARTIST
	};
	
	private static final String SELECTION_ALBUM = MediaStore.Audio.Media.ALBUM_ID + "=?";
	
	private MusicItem mItem;
	
	private TextView mSubtitle;
	private ImageView mImage;
    private ListView mListView;
	
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
		if (mItem.getType() != MusicItem.TYPE_ARTIST) {
			getLoaderManager().initLoader(0, null, this);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_more_dialog, container, false);
		
		TextView title = (TextView) v.findViewById(R.id.title);
		title.setText(mItem.getTitle());
		mSubtitle = (TextView) v.findViewById(R.id.subtitle);
		mImage = (ImageView) v.findViewById(R.id.image);
		
		mListView = (ListView) v.findViewById(R.id.list);
        mListView.setAdapter(mMenuAdapter);
        mListView.setOnItemClickListener(this);
		
		if (mItem.getType() == MusicItem.TYPE_ARTIST) {
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
		final long itemId = mItem.getId();
		final Uri uri;
		String[] projection = null;
		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = null;
		switch (mItem.getType()) {
		case MusicItem.TYPE_TRACK:
			uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, itemId);
			projection = PROJECTION_TRACK;
			break;
		case MusicItem.TYPE_ALBUM:
			uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			projection = PROJECTION_ALBUM;
			selection = SELECTION_ALBUM;
			selectionArgs = new String[] { String.valueOf(itemId) };
			sortOrder = MediaStore.Audio.Media.ARTIST_KEY;
			break;
		case MusicItem.TYPE_PLAYLIST:
			uri = MediaStore.Audio.Playlists.Members.getContentUri("external", itemId);
			projection = PROJECTION_PLAYLIST;
			sortOrder = MediaStore.Audio.Playlists.Members.PLAY_ORDER;
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
		
		switch(mItem.getType()) {
		case MusicItem.TYPE_TRACK:
			loadTrack(data);
			break;
		case MusicItem.TYPE_ALBUM:
			loadAlbum(data);	
			break;
		case MusicItem.TYPE_PLAYLIST:
			loadPlaylist(data);
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
            mSubtitle.setVisibility(View.VISIBLE);
			mSubtitle.setText(artist);
			
			// Load image
            if (AppUtils.isPortrait(getResources())) {
                loadImage(SoundstageUris.albumImage(albumId, album, artist));
            }
			
			// Add menu entries
            mMenuAdapter.clear();
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
			
			// Collect the set of artists for the album
			final List<MusicItem> artists = new ArrayList<MusicItem>();
			long lastArtistId = 0;			
			do {
				final long artistId = cursor.getLong(artistIdColIdx);
				if (artistId != lastArtistId) {
					artists.add(new MusicItem(
							artistId,	cursor.getString(artistColIdx), MusicItem.TYPE_ARTIST));
					lastArtistId = artistId;
				}
			} while (cursor.moveToNext());
			
			// Set the subtitle
            mSubtitle.setVisibility(View.VISIBLE);
			if (artists.size() == 1) {
				mSubtitle.setText(artists.get(0).getTitle());
			} else if (artists.size() > 1) {
				mSubtitle.setText(R.string.various_artists);
			}
			
			// Load the image for the album
            if (AppUtils.isPortrait(getResources())) {
                Uri uri = SoundstageUris.albumImage(mItem.getId(), mItem.getTitle(),
                        artists.get(0).getTitle());
                loadImage(uri);
            }
			
			// Add the option to the menu...
            mMenuAdapter.clear();
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
        if (AppUtils.isPortrait(getResources())) {
            loadImage(SoundstageUris.artistImage(mItem.getId(), mItem.getTitle()));
        }

		// Add menu entries
        mMenuAdapter.clear();
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
	
	private void loadPlaylist(Cursor cursor) {
		
	}

    private void loadImage(Uri uri) {
        final int width = mImage.getWidth();
        final int height = getResources().getDimensionPixelSize(R.dimen.more_dialog_image_height);
        if (width > 0) {
            Pablo.with(getActivity())
                    .load(uri)
                    .resize(width, height)
                    .centerCrop()
                    .into(mImage);
        } else {
            new DeferredWidthRequestCreator(Pablo.with(getActivity())
                    .load(uri)
                    .centerCrop(), mImage, height);
        }
    }
	
	private void playItem() {
		getMusicConnection().enqueue(mItem, MusicService.PLAY);
		getDialog().dismiss();
		showPlayer();
	}
	
	private AlertDialog buildArtistListDialog(List<MusicItem> artists) {
		final ListAdapter adapter = new ArrayAdapter<MusicItem>(getActivity(), 
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
		getMusicConnection().enqueue(mItem, MusicService.LAST);
	}
	
	private void addItemToPlayList() {
		// TODO
	}	
	
	private void showDeleteDialog() {
		String title = null;
		String message = null;
		switch (mItem.getType()) {
		case MusicItem.TYPE_TRACK:
			title = "Delete track";
			message = "Are you sure you want to delete the track '" + mItem.getTitle() + "' from your music collection?";
			break;
		case MusicItem.TYPE_ALBUM:
			title = "Delete album";
			message = "Are you sure you want to delete the album '" + mItem.getTitle() + "' from your music collection?";
			break;
		case MusicItem.TYPE_ARTIST:
			title = "Delete artist";
			message = "Are you sure you want to delete all tracks by the artist '" + mItem.getTitle() + "' from your music collection?";
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
		// TODO
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
