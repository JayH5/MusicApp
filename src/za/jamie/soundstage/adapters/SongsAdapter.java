package za.jamie.soundstage.adapters;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.TrackAdapter;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.TextView;


public class SongsAdapter extends TrackAdapter {

	public SongsAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final TextView title = (TextView) view.findViewById(R.id.title);
		final TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
		
		title.setText(cursor.getString(getTitleColIdx()));
		subtitle.setText(cursor.getString(getArtistColIdx()));
		
	}
	
	
}