package za.jamie.soundstage.adapters.artistbrowser;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.BasicTrackAdapter;
import za.jamie.soundstage.adapters.utils.FlippingViewHelper;
import za.jamie.soundstage.models.MusicItem;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.TextView;

public class ArtistTrackListAdapter extends BasicTrackAdapter {
	
	private FlippingViewHelper mFlipHelper;
	
	public ArtistTrackListAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
	}
	
	public void setFlippingViewHelper(FlippingViewHelper helper) {
		mFlipHelper = helper;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView titleText = (TextView) view.findViewById(R.id.title);
		TextView subtitleText = (TextView) view.findViewById(R.id.subtitle);
		
		String title = cursor.getString(getTitleColIdx());
		titleText.setText(title);
		subtitleText.setText(cursor.getString(getAlbumColIdx()));
		
		if (mFlipHelper != null) {
			MusicItem item = new MusicItem(cursor.getLong(getIdColIdx()), title, MusicItem.TYPE_TRACK);
			mFlipHelper.bindFlippedViewButtons(view, item);
		}
	}

}
