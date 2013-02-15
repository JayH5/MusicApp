package com.jamie.play.fragments.artistbrowser;

import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jamie.play.R;
import com.jamie.play.activities.AlbumBrowserActivity;
import com.jamie.play.adapters.abs.AlbumAdapter;
import com.jamie.play.bitmapfun.ImageFetcher;
import com.jamie.play.utils.ImageUtils;
import com.jamie.play.utils.TextUtils;

public class ArtistAlbumListFragment extends ListFragment implements 
		LoaderManager.LoaderCallbacks<Cursor> {
	
	public static final String EXTRA_ARTIST_ID = "extra_artist_id";
	
	private long mArtistId;
	
	private ImageFetcher mImageWorker;
	private AlbumAdapter mAdapter;
	
	public ArtistAlbumListFragment() {}
	
	public static ArtistAlbumListFragment newInstance(long artistId) {
		Bundle args = new Bundle();
		args.putLong(EXTRA_ARTIST_ID, artistId);
		
		ArtistAlbumListFragment frag = new ArtistAlbumListFragment();
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        mArtistId = getArguments().getLong(EXTRA_ARTIST_ID);
        
        mImageWorker = ImageUtils.getImageFetcher(getActivity());
        
        mAdapter = new ArtistAlbumsAdapter(getActivity(), 
        		R.layout.list_item_artist_album, null, 0);
        
        setListAdapter(mAdapter);
        
        /*CursorManager cm = new CursorManager(getActivity(), mAdapter, 
        		CursorDefinitions.getArtistAlbumsCursorParams(mArtist));
        getLoaderManager().initLoader(1, null, cm);*/
        
        getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mImageWorker.setExitTasksEarly(false);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mImageWorker.setExitTasksEarly(true);
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		Cursor cursor = (Cursor) mAdapter.getItem(position);

		final String album = cursor.getString(mAdapter.getAlbumColIdx());
		final String artist = cursor.getString(mAdapter.getArtistColIdx());
		
		final Intent i = new Intent(getActivity(), AlbumBrowserActivity.class);		
		i.putExtra(AlbumBrowserActivity.EXTRA_ALBUM_ID, id);
		i.putExtra(AlbumBrowserActivity.EXTRA_ALBUM, album);
		i.putExtra(AlbumBrowserActivity.EXTRA_ARTIST, artist);
		
		startActivity(i);
	}
	
	private class ArtistAlbumsAdapter extends AlbumAdapter {

		private int mNumTracksColIdx;
		private int mFirstYearColIdx;
		private int mLastYearColIdx;
		
		public ArtistAlbumsAdapter(Context context, int layout,
				Cursor c, int flags) {
			super(context, layout, c, flags);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final ImageView albumArt = (ImageView) view.findViewById(R.id.albumThumb);
			final TextView titleText = (TextView) view.findViewById(R.id.albumName);
			final TextView yearText = (TextView) view.findViewById(R.id.albumYear);
			final TextView numTracksText = (TextView) view.findViewById(R.id.albumTracks);
			
			String album = cursor.getString(getAlbumColIdx());
			titleText.setText(album);
			
			long albumId = cursor.getLong(getIdColIdx());
			mImageWorker.loadAlbumImage(albumId, albumArt);
			
			final Resources res = getResources();
			
			int numTracks = cursor.getInt(mNumTracksColIdx);
			numTracksText.setText(TextUtils.getNumTracksText(res, numTracks));
			
			int firstYear = cursor.getInt(mFirstYearColIdx);
			int lastYear = cursor.getInt(mLastYearColIdx);
			yearText.setText(TextUtils.getYearText(res, firstYear, lastYear));
		}
		
		@Override
		public void getColumnIndices(Cursor cursor) {
			super.getColumnIndices(cursor);
			if (cursor != null) {
				mNumTracksColIdx = cursor
						.getColumnIndexOrThrow(
								MediaStore.Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST);
				mFirstYearColIdx = cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Albums.FIRST_YEAR);
				mLastYearColIdx = cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Albums.LAST_YEAR);
			}
		}
		
	}
	
	private static class ArtistAlbumCursorLoader extends CursorLoader {

		public ArtistAlbumCursorLoader(Context context, Uri uri,
				String[] projection, String selection, String[] selectionArgs,
				String sortOrder) {
			super(context, uri, projection, selection, selectionArgs, sortOrder);
		}
		
		@Override
		public Cursor loadInBackground() {
			final Cursor tracksCursor = super.loadInBackground();
			
			if (tracksCursor != null) {
				final Map<String, Album> albumMap = new TreeMap<String, Album>();
				
				if (tracksCursor.moveToFirst()) {
					int albumIdColIdx = tracksCursor
							.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
					int albumColIdx = tracksCursor
							.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
					int artistColIdx = tracksCursor
							.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
					int yearColIdx = tracksCursor
							.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR);
					
					
					do {
						final String album = tracksCursor.getString(albumColIdx);
						Album albumHolder = albumMap.get(album);
						if (albumHolder != null) {
							albumHolder.update(tracksCursor.getInt(yearColIdx));
						} else {
							albumHolder = new Album(album, // album title
									tracksCursor.getLong(albumIdColIdx), // id
									tracksCursor.getString(artistColIdx), // artist
									tracksCursor.getInt(yearColIdx)); // year
							
							albumMap.put(album, albumHolder);
						}
					} while (tracksCursor.moveToNext());
				}
				tracksCursor.close();
				
				String[] projection = new String[] {
					MediaStore.Audio.Albums._ID,
					MediaStore.Audio.Albums.ALBUM,
					MediaStore.Audio.Albums.ARTIST,
					MediaStore.Audio.Albums.FIRST_YEAR,
					MediaStore.Audio.Albums.LAST_YEAR,
					MediaStore.Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST
				};
				
				MatrixCursor albumsCursor = new MatrixCursor(projection);
				
				for (Map.Entry<String, Album> entry : albumMap.entrySet()) {
					albumsCursor.addRow(entry.getValue().toColumnValues());
				}
				
				return albumsCursor;
			}
			return null;
		}
		
		private class Album {
			private long mId;
			private String mTitle;
			private String mArtist;
			private int mFirstYear;
			private int mLastYear;
			private int mTracks = 1;
			
			Album(String title, long id, String artist, int year) {
				mId = id;
				mTitle = title;
				mArtist = artist;
				mFirstYear = year;
				mLastYear = year;
			}
			
			public void update(int year) {
				mFirstYear = Math.min(year, mFirstYear);
				mLastYear = Math.max(year, mLastYear);
				mTracks++;
			}
			
			public Object[] toColumnValues() {
				return new Object[] {
					mId,
					mTitle,
					mArtist,
					mFirstYear,
					mLastYear,
					mTracks
				};
			}
		}
		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new ArtistAlbumCursorLoader(getActivity(), 
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
				new String[] {
					MediaStore.Audio.Media.ARTIST_ID,
					MediaStore.Audio.Media.ALBUM_ID,
					MediaStore.Audio.Media.ALBUM,
					MediaStore.Audio.Media.ARTIST,
					MediaStore.Audio.Media.YEAR,
				}, MediaStore.Audio.Media.ARTIST_ID + "=" + mArtistId, 
				null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);		
	}

}
