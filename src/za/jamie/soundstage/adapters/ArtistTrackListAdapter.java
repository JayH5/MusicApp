package za.jamie.soundstage.adapters;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.BasicTrackAdapter;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.TextView;

public class ArtistTrackListAdapter extends BasicTrackAdapter {
	
	public ArtistTrackListAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView titleText = (TextView) view.findViewById(R.id.title);
		TextView subtitleText = (TextView) view.findViewById(R.id.subtitle);
		
		titleText.setText(cursor.getString(getTitleColIdx()));
		subtitleText.setText(cursor.getString(getAlbumColIdx()));
	}

}