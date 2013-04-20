package za.jamie.soundstage.adapters;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.TrackAdapter;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

public class PlaylistTrackAdapter extends TrackAdapter {

	private int mIdColIdx;
	
	public PlaylistTrackAdapter(Context context, int layout, Cursor c, 
			int flags) {
		
		super(context, layout, c, flags);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final TextView titleText = (TextView) view.findViewById(R.id.title);
		final TextView subtitleText = (TextView) view.findViewById(R.id.subtitle);
		
		titleText.setText(cursor.getString(getTitleColIdx()));
		subtitleText.setText(cursor.getString(getArtistColIdx()));
	}
	
	@Override
	public int getIdColIdx() {
		return mIdColIdx;
	}
	
	@Override
	public void getColumnIndices(Cursor cursor) {
		super.getColumnIndices(cursor);
		if (cursor != null) {
			mIdColIdx = cursor
					.getColumnIndexOrThrow(
							MediaStore.Audio.Playlists.Members.AUDIO_ID);
		}
	}

}
