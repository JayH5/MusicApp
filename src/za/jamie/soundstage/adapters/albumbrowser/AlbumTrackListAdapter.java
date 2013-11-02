package za.jamie.soundstage.adapters.albumbrowser;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.BasicTrackAdapter;
import za.jamie.soundstage.adapters.utils.FlippingViewHelper;
import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.utils.TextUtils;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

public class AlbumTrackListAdapter extends BasicTrackAdapter {

	private int mTrackNumColIdx;
	private boolean mIsCompilation = false;
	
	private FlippingViewHelper mFlipHelper;
	
	public AlbumTrackListAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
	}
	
	public void setFlippingViewHelper(FlippingViewHelper helper) {
		mFlipHelper = helper;
	}

	@Override
	protected void getColumnIndices(Cursor cursor) {
		super.getColumnIndices(cursor);
		mTrackNumColIdx = cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK);
		
		// Search to determine if there is more than one artist, if so mark as compilation
		// so we can display artist name instead of track length.
		if (cursor.moveToFirst()) {
			final long artistId = cursor.getLong(getArtistIdColIdx());
			while (cursor.moveToNext()) {
				if (artistId != cursor.getLong(getArtistIdColIdx())) {
					mIsCompilation = true;
					break;
				}
			}
		}
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {			
		TextView titleText = (TextView) view.findViewById(R.id.title);
		TextView subtitleText = (TextView) view.findViewById(R.id.subtitle);
		TextView trackNumText = (TextView) view.findViewById(R.id.trackNumber);
		
		String title = cursor.getString(getTitleColIdx());
		titleText.setText(title);
		
		if (mIsCompilation) {
			subtitleText.setText(cursor.getString(getArtistColIdx()));
		} else {
			long duration = cursor.getLong(getDurationColIdx());
			subtitleText.setText(TextUtils.getTrackDurationText(duration));
		}
		
		int trackNum = cursor.getInt(mTrackNumColIdx);
		trackNumText.setText(TextUtils.getTrackNumText(trackNum));
		
		if (mFlipHelper != null) {
			MusicItem item = new MusicItem(cursor.getLong(getIdColIdx()), title, MusicItem.TYPE_TRACK);
			mFlipHelper.bindFlippedViewButtons(view, item);
		}
	}
	
}
