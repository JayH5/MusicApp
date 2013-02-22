package za.jamie.soundstage.fragments.artistbrowser;

import java.util.List;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.AlbumBrowserActivity;
import za.jamie.soundstage.adapters.abs.ResourceArrayAdapter;
import za.jamie.soundstage.bitmapfun.ImageFetcher;
import za.jamie.soundstage.loaders.ArtistAlbumListLoader;
import za.jamie.soundstage.models.ArtistAlbum;
import za.jamie.soundstage.utils.ImageUtils;
import za.jamie.soundstage.utils.TextUtils;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ArtistAlbumListFragment extends ListFragment implements 
		LoaderManager.LoaderCallbacks<List<ArtistAlbum>> {
	
	public static final String EXTRA_ARTIST_ID = "extra_artist_id";
	public static final String EXTRA_ARTIST = "extra_artist";
	
	private long mArtistId;
	private String mArtist;
	
	private ImageFetcher mImageWorker;
	private ArtistAlbumAdapter mAdapter;
	
	public ArtistAlbumListFragment() {}
	
	public static ArtistAlbumListFragment newInstance(long artistId, String artist) {
		Bundle args = new Bundle();
		args.putLong(EXTRA_ARTIST_ID, artistId);
		args.putString(EXTRA_ARTIST, artist);
		
		ArtistAlbumListFragment frag = new ArtistAlbumListFragment();
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        mArtistId = getArguments().getLong(EXTRA_ARTIST_ID);
        mArtist = getArguments().getString(EXTRA_ARTIST);
        
        mImageWorker = ImageUtils.getImageFetcher(getActivity());
        
        /*mAdapter = new ArtistAlbumsAdapter(getActivity(), 
        		R.layout.list_item_artist_album, null, 0);*/
        
        mAdapter = new ArtistAlbumAdapter(getActivity(), 
        		R.layout.list_item_artist_album, null);
        
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
		ArtistAlbum artistAlbum = (ArtistAlbum) mAdapter.getItem(position);
		
		final Intent i = new Intent(getActivity(), AlbumBrowserActivity.class);		
		i.putExtra(AlbumBrowserActivity.EXTRA_ALBUM_ID, id);
		i.putExtra(AlbumBrowserActivity.EXTRA_ALBUM, artistAlbum.album);
		i.putExtra(AlbumBrowserActivity.EXTRA_ARTIST, mArtist);
		
		startActivity(i);
	}
	
	/*private class ArtistAlbumsAdapter extends AlbumAdapter {

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
		
	}*/
	
	private class ArtistAlbumAdapter extends ResourceArrayAdapter<ArtistAlbum> {

		public ArtistAlbumAdapter(Context context, int resource,
				List<ArtistAlbum> objects) {
			super(context, resource, objects);
		}

		@Override
		public void bindView(View view, Context context, ArtistAlbum artistAlbum) {
			final ImageView albumArt = (ImageView) view.findViewById(R.id.albumThumb);
			final TextView titleText = (TextView) view.findViewById(R.id.albumName);
			final TextView yearText = (TextView) view.findViewById(R.id.albumYear);
			final TextView numTracksText = (TextView) view.findViewById(R.id.albumTracks);
			
			titleText.setText(artistAlbum.album);
			mImageWorker.loadAlbumImage(artistAlbum.albumId, albumArt);
			
			final Resources res = getResources();
			numTracksText.setText(TextUtils.getNumTracksText(res, artistAlbum.numTracks));
			yearText.setText(TextUtils.getYearText(res, artistAlbum.firstYear, 
					artistAlbum.lastYear));
		}

		
	}
	
	/*private static class ArtistAlbumCursorLoader extends CursorLoader {

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
		
	}*/

	@Override
	public Loader<List<ArtistAlbum>> onCreateLoader(int id, Bundle args) {
		return new ArtistAlbumListLoader(getActivity(), mArtistId);
	}

	@Override
	public void onLoadFinished(Loader<List<ArtistAlbum>> loader, List<ArtistAlbum> data) {
		mAdapter.setList(data);	
	}

	@Override
	public void onLoaderReset(Loader<List<ArtistAlbum>> arg0) {
		mAdapter.setList(null);	
	}

}
