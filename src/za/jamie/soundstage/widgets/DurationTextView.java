package za.jamie.soundstage.widgets;

import za.jamie.soundstage.utils.TextUtils;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class DurationTextView extends TextView {

	public DurationTextView(Context context) {
		super(context);
	}

	public DurationTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DurationTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setDuration(long duration) {
		setText(TextUtils.getTrackDurationText(duration));
	}

}
