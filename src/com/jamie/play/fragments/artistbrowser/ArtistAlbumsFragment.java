package com.jamie.play.fragments.artistbrowser;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jamie.play.R;
import com.jamie.play.activities.AlbumBrowserActivity;
import com.jamie.play.adapters.AlbumAdapter;
import com.jamie.play.bitmapfun.ImageFetcher;
import com.jamie.play.cursormanager.CursorDefinitions;
import com.jamie.play.cursormanager.CursorManager;
import com.jamie.play.utils.ImageUtils;

public class ArtistAlbumsFragment extends ListFragment {
	
	public static final String EXTRA_ARTIST = "extra_artist";
	
	private String mArtist;
	
	private ImageFetcher mImageWorker;
	private AlbumAdapter mAdapter;
	
	public ArtistAlbumsFragment() {}
	
	public static ArtistAlbumsFragment newInstance(String artist) {
		Bundle args = new Bundle();
		args.putString(EXTRA_ARTIST, artist);
		
		ArtistAlbumsFragment frag = new ArtistAlbumsFragment();
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        mArtist = getArguments().getString(EXTRA_ARTIST);
        
        mImageWorker = ImageUtils.getImageFetcher(getActivity());
        
        mAdapter = new ArtistAlbumsAdapter(getActivity(), 
        		R.layout.list_item_artist_album, null, 0);
        
        setListAdapter(mAdapter);
        
        CursorManager cm = new CursorManager(getActivity(), mAdapter, 
        		CursorDefinitions.getArtistAlbumsCursorParams(mArtist));
        getLoaderManager().initLoader(1, null, cm);
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
			mImageWorker.loadAlbumImage(albumId, mArtist, album, albumArt);
			
			int numTracks = cursor.getInt(mNumTracksColIdx);
			if (numTracks > 1) {
				numTracksText.setText(numTracks + " TRACKS");
			} else {
				numTracksText.setText(numTracks + " TRACK");
			}
			
			int firstYear = cursor.getInt(mFirstYearColIdx);
			int lastYear = cursor.getInt(mLastYearColIdx);
			String yearString = "";
			if (firstYear > 0) {
				yearString += firstYear;
				if (lastYear > 0 && firstYear != lastYear) {
					yearString += " - " + lastYear;
				}
			} else if (lastYear > 0) {
				yearString += lastYear;
			}
			yearText.setText(yearString);
		}
		
		@Override
		public void getColumnIndices(Cursor cursor) {
			super.getColumnIndices(cursor);
			Log.d("ArtistAlbumsFragment", "Cursor for artist: " + mArtist);
			if (cursor != null) {
				String[] colNames = cursor.getColumnNames();
				for (String colName : colNames) {
					Log.d("ArtistAlbumsFragment", "Cursor column: " + colName);
				}
				
				mNumTracksColIdx = cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS);
				mFirstYearColIdx = cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Albums.FIRST_YEAR);
				mLastYearColIdx = cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Albums.LAST_YEAR);
			}
		}
		
	}

}
