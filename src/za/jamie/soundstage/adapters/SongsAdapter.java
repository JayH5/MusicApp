package za.jamie.soundstage.adapters;

import java.util.ArrayList;
import java.util.List;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.BasicTrackAdapter;
import za.jamie.soundstage.utils.TextUtils;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;


public class SongsAdapter extends BasicTrackAdapter implements SectionIndexer {

	private String[] mSectionHeaders;
	private Integer[] mSectionPositions;
	private Object[] mData;
	
	private final LayoutInflater mInflater;
    private int mHeaderLayout;
	
	public SongsAdapter(Context context, int layout, int headerLayout, Cursor c, int flags) {
		super(context, layout, c, flags);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mHeaderLayout = headerLayout;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final TextView title = (TextView) view.findViewById(R.id.title);
		final TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
		
		title.setText(cursor.getString(getTitleColIdx()));
		subtitle.setText(cursor.getString(getArtistColIdx()));		
	}
	
	@Override
	protected void onCursorLoad(Cursor cursor) {
		super.onCursorLoad(cursor);
		
		if (cursor.moveToFirst()) {
			String previousHeader = null;
			List<Object> data = new ArrayList<Object>();
			List<String> sections = new ArrayList<String>();
			List<Integer> positions = new ArrayList<Integer>();
			final int titleColIdx = getTitleColIdx();
			int count = 0;
			do {
				final String title = cursor.getString(titleColIdx);
				final String header = TextUtils.headerFor(title);
				if (!header.equals(previousHeader)) {
					sections.add(header);
					positions.add(count);
					
					previousHeader = header;
					
					data.add(header);
					count++;
				}
				data.add(cursor.getPosition());
				count++;
			} while (cursor.moveToNext());
			
			mData = data.toArray();
			
			mSectionHeaders = new String[sections.size()];
			sections.toArray(mSectionHeaders);
			
			mSectionPositions = new Integer[positions.size()];
			positions.toArray(mSectionPositions);
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (mData == null || position < 0 || position >= mData.length) {
            return null;
        }
		
		final Object item = getItem(position);
		View v = null;
		
		if (item instanceof String) { // Header
			final String header = (String) item;
			if (convertView == null || convertView.findViewById(R.id.header) == null ) {
				v = newView(mContext, header, parent);
			} else {
				v = convertView;
			}
			bindView(v, mContext, header);
		} else if (item instanceof Cursor) { // Cursor position
			final Cursor cursor = (Cursor) item;
			if (convertView == null || convertView.findViewById(R.id.title) == null) {
                v = newView(mContext, cursor, parent);
            } else {
            	v = convertView;
            }            
            bindView(v, mContext, cursor);
		}
		return v;
	}
	
	public View newView(Context context, String header, ViewGroup parent) {
		return mInflater.inflate(mHeaderLayout, parent, false);
	}
	
	public void bindView(View view, Context context, String headerText) {
		final TextView headerView = (TextView) view.findViewById(R.id.header);
		headerView.setText(headerText);
	}

	@Override
	public int getPositionForSection(int section) {
		return (mSectionPositions != null) ? (Integer) mSectionPositions[section] : 0;
	}

	@Override
	public int getSectionForPosition(int position) {
		if (mSectionPositions != null) {
            for (int i = 0; i < mSectionPositions.length - 1; i++) {
                if (position >= (Integer) mSectionPositions[i]
                        && position < (Integer) mSectionPositions[i + 1]) {
                    return i;
                }
            }
            if (position >= (Integer)mSectionPositions[mSectionPositions.length - 1]) {
                return mSectionPositions.length - 1;
            }
        }
        return 0;
	}

	@Override
	public Object[] getSections() {
		return mSectionHeaders;
	}	
	
	@Override
    public int getCount() {
    	return (mData != null) ? mData.length : 0; 
    }
    
    @Override
    public long getItemId(int position) {
    	Object object = getItem(position);
    	if (object instanceof Cursor) {
    		return ((Cursor) object).getLong(getIdColIdx());
    	} else {
    		return 0;
    	}
    }
    
    @Override
    public Object getItem(int position) {
    	if (mData != null) {
    		Object item = mData[position];
    		if (item instanceof String) {
    			return (String) item;
    		} else if (item instanceof Integer) {
    			Cursor cursor = getCursor();
    			cursor.moveToPosition((Integer) item);
    			return cursor;
    		}
    	}
    	return null;
    }
    
    @Override
    public boolean areAllItemsEnabled() {
    	return false;
    }
    
    @Override
    public boolean isEnabled(int position) {
    	return (mData != null) ? (mData[position] instanceof Integer) : false;
    }
    
    public int getRealPosition(int position) {
    	return position - getSectionForPosition(position) - 1;
    }

}