package za.jamie.soundstage.adapters;

import java.util.List;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.ResourceArrayAdapter;
import za.jamie.soundstage.models.Track;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class PlayQueueAdapter extends ResourceArrayAdapter<Track> {

	public PlayQueueAdapter(Context context, int resource, List<Track> objects) {
		super(context, resource, objects);
	}
	
	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}

	@Override
	public void bindView(View view, Context context, Track object) {
		final TextView titleText = (TextView) view.findViewById(R.id.title);
		final TextView subtitleText = (TextView) view.findViewById(R.id.subtitle);
		
		titleText.setText(object.getTitle());
		subtitleText.setText(object.getArtist());
	}

}
