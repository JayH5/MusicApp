package za.jamie.soundstage.adapters.abs;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * An abstract adapter that uses the newView/bindView pattern from the
 * Android CursorAdapter.
 * See: http://gist.github.com/JakeWharton/5423616
 * @author Jamie Hewland
 *
 */
public abstract class BindingAdapter extends BaseAdapter {

	protected Context mContext;
	
	public BindingAdapter(Context context) {
		mContext = context;
	}
	
	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = newView(mContext, position, parent);
		}
		bindView(mContext, position, convertView);
		return convertView;
	}
	
	public abstract View newView(Context context, int position, ViewGroup parent);
	
	public abstract void bindView(Context context, int position, View view);

}
