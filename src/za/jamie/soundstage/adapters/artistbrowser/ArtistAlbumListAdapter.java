package za.jamie.soundstage.adapters.artistbrowser;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.BasicCursorAdapter;
import za.jamie.soundstage.adapters.utils.FlippingViewHelper;
import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.pablo.LastfmUris;
import za.jamie.soundstage.pablo.Pablo;
import za.jamie.soundstage.utils.TextUtils;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ArtistAlbumListAdapter extends BasicCursorAdapter {

	private int mIdColIdx;
	private int mAlbumColIdx;
	private int mArtistColIdx;
	private int mNumSongsForArtistColIdx;
	private int mFirstYearColIdx;
	private int mLastYearColIdx;
	
	private FlippingViewHelper mFlipHelper;
	
	private final Context mContext;
	
	public ArtistAlbumListAdapter(Context context, int layout, Cursor c,
			int flags) {			
		super(context, layout, c, flags);
		mContext = context;
	}
	
	public void setFlippingViewHelper(FlippingViewHelper helper) {
		mFlipHelper = helper;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView nameText = (TextView) view.findViewById(R.id.albumName);
		TextView tracksText = (TextView) view.findViewById(R.id.albumTracks);
		TextView yearText = (TextView) view.findViewById(R.id.albumYear);
		ImageView thumbImage = (ImageView) view.findViewById(R.id.albumThumb);
		
		String album = cursor.getString(mAlbumColIdx);
		nameText.setText(album);
		
		final Resources res = context.getResources();
		tracksText.setText(TextUtils.getNumTracksText(res, 
				cursor.getInt(mNumSongsForArtistColIdx)));
		
		yearText.setText(TextUtils.getYearText(cursor.getInt(mFirstYearColIdx), 
				cursor.getInt(mLastYearColIdx)));
		
		String artist = cursor.getString(mArtistColIdx);
		long id = cursor.getLong(mIdColIdx);
		Uri uri = LastfmUris.getAlbumInfoUri(album, artist, id);
		
		Pablo.with(mContext)
			.load(uri)
            .resizeDimen(R.dimen.image_thumb_album, R.dimen.image_thumb_album)
            .placeholder(R.drawable.placeholder_grey)
			.into(thumbImage);
		
		if (mFlipHelper != null) {
			MusicItem item = new MusicItem(id, album, MusicItem.TYPE_ALBUM);
			mFlipHelper.bindFlippedViewButtons(view, item);
		}
	}
	
	@Override
	protected void getColumnIndices(Cursor cursor) {
		mIdColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID);
		mAlbumColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
		mArtistColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST);
		mNumSongsForArtistColIdx = cursor.getColumnIndexOrThrow(
				MediaStore.Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST);
		mFirstYearColIdx = cursor.getColumnIndexOrThrow(
				MediaStore.Audio.Albums.FIRST_YEAR);
		mLastYearColIdx = cursor.getColumnIndexOrThrow(
				MediaStore.Audio.Albums.LAST_YEAR);
	}

}
