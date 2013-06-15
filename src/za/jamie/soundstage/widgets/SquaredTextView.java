package za.jamie.soundstage.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class SquaredTextView extends TextView {

	public SquaredTextView(Context context) {
		super(context);
	}

	public SquaredTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SquaredTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override 
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
	}

}
