package za.jamie.soundstage.adapters;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.TrackAdapter;
import za.jamie.soundstage.utils.TextUtils;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

public class AlbumTrackListAdapter extends TrackAdapter {

	private int mTrackNumColIdx;
	
	public AlbumTrackListAdapter(Context context, int layout, Cursor c,
			int flags) {
		
		super(context, layout, c, flags);
	}

	@Override
	protected void getColumnIndices(Cursor cursor) {
		super.getColumnIndices(cursor);
		mTrackNumColIdx = cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final TextView titleText = (TextView) 
				view.findViewById(R.id.title);
		final TextView durationText = (TextView) 
				view.findViewById(R.id.subtitle);
		final TextView trackNumText = (TextView) 
				view.findViewById(R.id.trackNumber);
		
		// Set the track name
		titleText.setText(cursor.getString(getTitleColIdx()));
		
		// Set the duration
		long duration = cursor.getLong(getDurationColIdx());
		durationText.setText(TextUtils.getTrackDurationText(duration));
		
		// Set the track number
		int trackNum = cursor.getInt(mTrackNumColIdx);
		trackNumText.setText(TextUtils.getTrackNumText(trackNum));
	}
	
}