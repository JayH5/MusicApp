package com.jamie.play.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jamie.play.R;
import com.jamie.play.bitmapfun.ImageFetcher;
import com.jamie.play.utils.TextUtils;

public class ArtistsAdapter extends ArtistAdapter {	
	private int mNumAlbumsIdx;
	private int mNumTracksIdx;
	
	private ImageFetcher mImageWorker;
	
	public ArtistsAdapter(Context context, int layout, Cursor c, int flags,
			ImageFetcher imageWorker) {
		super(context, layout, c, flags);
		mImageWorker = imageWorker;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {		
		final TextView title = (TextView) view.findViewById(R.id.artistName);
		final TextView numAlbumsText = (TextView) view.findViewById(R.id.artistAlbums);
		final TextView numTracksText = (TextView) view.findViewById(R.id.artistTracks);
		final ImageView artistImage = (ImageView) view.findViewById(R.id.artistImage);
		
		final String artist = cursor.getString(getArtistColIdx());
		final int numAlbums = cursor.getInt(mNumAlbumsIdx);
		final int numTracks = cursor.getInt(mNumTracksIdx);
		
		title.setText(artist);
		
		final Resources res = context.getResources();
		numAlbumsText.setText(TextUtils.getNumAlbumsText(res, numAlbums));
		numTracksText.setText(TextUtils.getNumTracksText(res, numTracks));
		
		final long artistId = cursor.getLong(getIdColIdx());
		
		mImageWorker.loadArtistImage(artistId, artist, artistImage);
	}
	
	@Override	
	public void getColumnIndices(Cursor cursor) {
		super.getColumnIndices(cursor);
		if (cursor != null) {
			mNumAlbumsIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
			mNumTracksIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
		}
	}

}
