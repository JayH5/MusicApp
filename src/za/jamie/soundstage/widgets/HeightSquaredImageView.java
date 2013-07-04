package za.jamie.soundstage.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/** 
 * An image view which always remains square with respect to its height. 
 */
public class HeightSquaredImageView extends ImageView {
	public HeightSquaredImageView(Context context) {
		super(context);
	}

	public HeightSquaredImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override 
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredHeight());
	}
}