package za.jamie.soundstage.adapters;

import java.util.Locale;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.TrackAdapter;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

public class AlbumTrackListAdapter extends TrackAdapter {

	private int mTrackNumColIdx;
	private int mDurationColIdx;
	
	public AlbumTrackListAdapter(Context context, int layout, Cursor c,
			int flags) {
		super(context, layout, c, flags);
	}

	@Override
	public void getColumnIndices(Cursor cursor) {
		super.getColumnIndices(cursor);
		
		if (cursor != null) {
			mTrackNumColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK);
			mDurationColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
		}
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
		long duration = cursor.getLong(mDurationColIdx);
		String durationString = String.format(Locale.US,
				"%02d:%02d", duration / 60000, (duration % 60000) / 1000);
		durationText.setText(durationString);
		
		// Set the track number
		int trackNum = cursor.getInt(mTrackNumColIdx);
		String trackNumString = String.format(Locale.US, 
				"%02d", trackNum % 100);
		trackNumText.setText(trackNumString);
	}
	
}