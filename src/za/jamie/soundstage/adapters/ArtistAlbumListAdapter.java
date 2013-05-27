package za.jamie.soundstage.adapters;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.AlbumAdapter;
import za.jamie.soundstage.bitmapfun.ImageFetcher;
import za.jamie.soundstage.utils.ImageUtils;
import za.jamie.soundstage.utils.TextUtils;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ArtistAlbumListAdapter extends AlbumAdapter {

	private int mNumSongsForArtistColIdx;
	private int mFirstYearColIdx;
	private int mLastYearColIdx;
	
	private Resources mResources;
	private ImageFetcher mImageWorker;
	
	public ArtistAlbumListAdapter(Context context, int layout, Cursor c,
			int flags) {
		
		super(context, layout, c, flags);
		mResources = context.getResources();
		mImageWorker = ImageUtils.getThumbImageFetcher(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView nameText = (TextView) view.findViewById(R.id.albumName);
		TextView tracksText = (TextView) view.findViewById(R.id.albumTracks);
		TextView yearText = (TextView) view.findViewById(R.id.albumYear);
		ImageView thumbImage = (ImageView) view.findViewById(R.id.albumThumb);
		
		nameText.setText(cursor.getString(getAlbumColIdx()));
		
		tracksText.setText(TextUtils.getNumTracksText(mResources, 
				cursor.getInt(mNumSongsForArtistColIdx)));
		
		yearText.setText(TextUtils.getYearText(cursor.getInt(mFirstYearColIdx), 
				cursor.getInt(mLastYearColIdx)));
		
		mImageWorker.loadAlbumImage(cursor.getLong(getIdColIdx()), thumbImage);
	}
	
	@Override
	protected void getColumnIndices(Cursor cursor) {
		super.getColumnIndices(cursor);
		
		mNumSongsForArtistColIdx = cursor.getColumnIndexOrThrow(
				MediaStore.Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST);
		mFirstYearColIdx = cursor.getColumnIndexOrThrow(
				MediaStore.Audio.Albums.FIRST_YEAR);
		mLastYearColIdx = cursor.getColumnIndexOrThrow(
				MediaStore.Audio.Albums.LAST_YEAR);
	}

}
