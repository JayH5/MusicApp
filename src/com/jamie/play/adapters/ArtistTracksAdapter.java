package com.jamie.play.adapters;

import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import com.jamie.play.R;

public class ArtistTracksAdapter extends TrackAdapter {

	private int mDurationColIdx;
	
	public ArtistTracksAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView titleText = (TextView) view.findViewById(R.id.title);
		TextView durationText = (TextView) view.findViewById(R.id.subtitle);
		
		titleText.setText(cursor.getString(getTitleColIdx()));
		
		long duration = cursor.getLong(mDurationColIdx);
		String durationString = String.format(Locale.US,
				"%02d:%02d", duration / 60000, (duration % 60000) / 1000);
		durationText.setText(durationString);		
	}
	
	@Override
	public void getColumnIndices(Cursor cursor) {
		super.getColumnIndices(cursor);
		if (cursor != null) {
			mDurationColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
		}
	}

}
