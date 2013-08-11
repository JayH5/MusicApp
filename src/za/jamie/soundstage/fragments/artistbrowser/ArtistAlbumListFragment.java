package za.jamie.soundstage.fragments.artistbrowser;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.BasicCursorAdapter;
import za.jamie.soundstage.adapters.wrappers.HeaderViewListAdapter;
import za.jamie.soundstage.bitmapfun.ImageFetcher;
import za.jamie.soundstage.fragments.MusicListFragment;
import za.jamie.soundstage.musicstore.CursorManager;
import za.jamie.soundstage.musicstore.MusicStore;
import za.jamie.soundstage.utils.ImageUtils;
import za.jamie.soundstage.utils.TextUtils;
import za.jamie.soundstage.utils.ViewUtils;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ArtistAlbumListFragment extends MusicListFragment {
	
	public static final String EXTRA_ARTIST_ID = "extra_artist_id";
	
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
        
        CursorAdapter adapter = new ArtistAlbumListAdapter(getActivity(), 
        		R.layout.list_item_artist_album, null, 0);
        
        ListAdapter listAdapter;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	HeaderViewListAdapter wrapper = new HeaderViewListAdapter(adapter);
        	View spacer = ViewUtils.createListSpacer(getActivity());
        	wrapper.addHeaderView(spacer);
        	wrapper.addFooterView(spacer);
        	listAdapter = wrapper;
        } else {
        	listAdapter = adapter;
        }
        
        setListAdapter(listAdapter);
        
        long artistId = getArguments().getLong(EXTRA_ARTIST_ID);
        CursorManager cm = new CursorManager(getActivity(), adapter, 
        		MusicStore.Albums.getArtistAlbums(artistId));
        
        getLoaderManager().initLoader(1, null, cm);
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {		
		final Uri data = ContentUris.withAppendedId(
				MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id);
		final Intent intent = new Intent(Intent.ACTION_VIEW)		
			.setDataAndType(data, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE);
		
		startActivity(intent);
	}
	
	private static class ArtistAlbumListAdapter extends BasicCursorAdapter {

		private int mIdColIdx;
		private int mAlbumColIdx;
		private int mNumSongsForArtistColIdx;
		private int mFirstYearColIdx;
		private int mLastYearColIdx;
		
		private ImageFetcher mImageWorker;
		
		public ArtistAlbumListAdapter(Context context, int layout, Cursor c,
				int flags) {			
			super(context, layout, c, flags);
			mImageWorker = ImageUtils.getThumbImageFetcher(context);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView nameText = (TextView) view.findViewById(R.id.albumName);
			TextView tracksText = (TextView) view.findViewById(R.id.albumTracks);
			TextView yearText = (TextView) view.findViewById(R.id.albumYear);
			ImageView thumbImage = (ImageView) view.findViewById(R.id.albumThumb);
			
			nameText.setText(cursor.getString(mAlbumColIdx));
			
			final Resources res = context.getResources();
			tracksText.setText(TextUtils.getNumTracksText(res, 
					cursor.getInt(mNumSongsForArtistColIdx)));
			
			yearText.setText(TextUtils.getYearText(cursor.getInt(mFirstYearColIdx), 
					cursor.getInt(mLastYearColIdx)));
			
			mImageWorker.loadAlbumImage(cursor.getLong(mIdColIdx), thumbImage);
		}
		
		@Override
		protected void getColumnIndices(Cursor cursor) {
			mIdColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID);
			mAlbumColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
			mNumSongsForArtistColIdx = cursor.getColumnIndexOrThrow(
					MediaStore.Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST);
			mFirstYearColIdx = cursor.getColumnIndexOrThrow(
					MediaStore.Audio.Albums.FIRST_YEAR);
			mLastYearColIdx = cursor.getColumnIndexOrThrow(
					MediaStore.Audio.Albums.LAST_YEAR);
		}

	}

}
