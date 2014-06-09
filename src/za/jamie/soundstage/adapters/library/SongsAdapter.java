package za.jamie.soundstage.adapters.library;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.LibraryAdapter;
import za.jamie.soundstage.adapters.utils.FlippingViewHelper;
import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.utils.TextUtils;

public class SongsAdapter extends LibraryAdapter {
	private int mIdColIdx;
	private int mTitleColIdx;
	private int mArtistIdColIdx;
	private int mArtistColIdx;
	private int mAlbumIdColIdx;
	private int mAlbumColIdx;
	private FlippingViewHelper mFlipHelper;

	public SongsAdapter(Context context, int layout, int headerLayout,
			Cursor c, int flags) {
		super(context, layout, headerLayout, c, flags);
	}

	public void setFlippingViewHelper(FlippingViewHelper helper) {
		mFlipHelper = helper;
	}

	@Override
	protected void getColumnIndices(Cursor cursor) {
		mIdColIdx = cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
		mTitleColIdx = cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
		mArtistIdColIdx = cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID);
		mArtistColIdx = cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
		mAlbumIdColIdx = cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
		mAlbumColIdx = cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
	}

	@Override
	protected String getSection(Context context, Cursor cursor) {
		return TextUtils.headerFor(cursor.getString(mTitleColIdx));
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView titleView = (TextView) view.findViewById(R.id.title);
		TextView subtitleView = (TextView) view.findViewById(R.id.subtitle);

		final String title = cursor.getString(mTitleColIdx);
		titleView.setText(title);
		subtitleView.setText(cursor.getString(mArtistColIdx));

		if (mFlipHelper != null) {
			MusicItem item = new MusicItem(cursor.getLong(mIdColIdx), title, MusicItem.TYPE_TRACK);
			mFlipHelper.bindFlippedViewButtons(view, item);
		}
	}

}