package za.jamie.soundstage.adapters.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.LibraryAdapter;
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

public class ArtistsAdapter extends LibraryAdapter {
	private int mArtistColIdx;
	private int mArtistIdColIdx;
	private int mNumAlbumsIdx;
	private int mNumTracksIdx;
	private FlippingViewHelper mFlipHelper;
	
	private final Context mContext;
	
	public ArtistsAdapter(Context context, int layout, int headerLayout,
			Cursor c, int flags) {
		super(context, layout, headerLayout, c, flags);
		mContext = context;
	}
	
	public void setFlippingViewHelper(FlippingViewHelper helper) {
		mFlipHelper = helper;
	}

	@Override
	protected void getColumnIndices(Cursor cursor) {
		mArtistColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);
		mArtistIdColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID);
		mNumAlbumsIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
		mNumTracksIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
	}

	@Override
	protected String getSection(Context context, Cursor cursor) {
		return TextUtils.headerFor(cursor.getString(mArtistColIdx));
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView title = (TextView) view.findViewById(R.id.artistName);
		TextView numAlbumsText = (TextView) view.findViewById(R.id.artistAlbums);
		TextView numTracksText = (TextView) view.findViewById(R.id.artistTracks);
		ImageView artistImage = (ImageView) view.findViewById(R.id.artistImage);
		
		String artist = cursor.getString(mArtistColIdx);
		title.setText(artist);
		
		final Resources res = context.getResources();
		numAlbumsText.setText(TextUtils.getNumAlbumsText(res, cursor.getInt(mNumAlbumsIdx)));
		numTracksText.setText(TextUtils.getNumTracksText(res, cursor.getInt(mNumTracksIdx)));
		
		Uri uri = LastfmUris.getArtistInfoUri(artist);
		Pablo.with(mContext)
			.load(uri)
			.resizeDimen(R.dimen.image_thumb_artist, R.dimen.image_thumb_artist)
			.centerCrop()
			.into(artistImage);
		
		if (mFlipHelper != null) {
			MusicItem item = new MusicItem(cursor.getLong(mArtistIdColIdx), artist, MusicItem.TYPE_ARTIST);
			mFlipHelper.bindFlippedViewButtons(view, item);
		}
	}
	
}
