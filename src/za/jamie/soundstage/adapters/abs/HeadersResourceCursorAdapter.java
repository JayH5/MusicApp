package za.jamie.soundstage.adapters.abs;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;

public abstract class HeadersResourceCursorAdapter extends BasicCursorAdapter 
		implements SectionIndexer {

	public static final int VIEW_TYPE_REGULAR = 0;
	public static final int VIEW_TYPE_HEADER = 1;
	
	private int mHeaderLayout;
	
	private int mMinSectionSize = 1;
	
	private int[] mPositions = null;
	private int[] mHeaderPositions = null;
	private int[] mViewTypes = null;
	private String[] mHeaders = null;
	
	private Context mContext;
	private LayoutInflater mInflater;
	
	public HeadersResourceCursorAdapter(Context context, int layout, int headerLayout, 
			Cursor c, int flags) {
		super(context, layout, c, flags);
		mHeaderLayout = headerLayout;
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (mViewTypes == null || position < 0 || position >= mViewTypes.length) {
			Log.e("Headers", "View types null/invalid position: " + position);
			return null;
		}
		
		View v = null;		
		switch (getItemViewType(position)) {
		case VIEW_TYPE_REGULAR:
			final Cursor cursor = getCursor();
			cursor.moveToPosition(mPositions[position]);
			if (convertView == null) {
				v = newView(mContext, cursor, parent);
			} else {
				v = convertView;
			}
			bindView(v, mContext, cursor);
			break;
		case VIEW_TYPE_HEADER:
			final String header = mHeaders[mPositions[position]];
			if (convertView == null) {
				v = newHeaderView(mContext, parent);
			} else {
				v = convertView;
			}
			bindHeaderView(v, mContext, header);
			break;
		}
		
		return v;
	}
	
	public View newHeaderView(Context context, ViewGroup parent) {
		return mInflater.inflate(mHeaderLayout, parent, false);
	}
	
	public abstract void bindHeaderView(View view, Context context, String header);
    
    public void setHeaderViewResource(int headerLayout) {
    	mHeaderLayout = headerLayout;
    }
    
    @Override
	public int getItemViewType(int position) {
		return mViewTypes != null ? mViewTypes[position] : VIEW_TYPE_REGULAR;
	}
	
	@Override
	public int getViewTypeCount() {
		return super.getViewTypeCount() + 1;
	}
	
	@Override
    public boolean areAllItemsEnabled() {
    	return false;
    }
	
	@Override
    public boolean isEnabled(int position) {
    	return (getItemViewType(position) != VIEW_TYPE_HEADER);
    }
	
	@Override
	public Object getItem(int position) {
		if (mViewTypes == null) {
			return null;
		}
		
		switch(mViewTypes[position]) {
		case VIEW_TYPE_REGULAR:
			return super.getItem(mPositions[position]);
		case VIEW_TYPE_HEADER:
			return mHeaders[mPositions[position]];
		default:
			return null;
		}
	}
	
	@Override
	public long getItemId(int position) {
		if (mViewTypes == null) {
			return 0;
		}
		
		switch(mViewTypes[position]) {
		case VIEW_TYPE_REGULAR:
			return super.getItemId(mPositions[position]);
		case VIEW_TYPE_HEADER:
			return 0;
		default:
			return 0;
		}
	}
	
	@Override
	public int getCount() {
		return mViewTypes != null ? mViewTypes.length : 0;
	}
	
	@Override
	public int getPositionForSection(int section) {
		return (mHeaderPositions != null) ? mHeaderPositions[section] : 0;
	}

	@Override
	public int getSectionForPosition(int position) {
		if (mViewTypes != null) {
			while (position >= 0 && mViewTypes[position] != VIEW_TYPE_HEADER) {
				position--;
			}
			if (position == 0 && mViewTypes[0] != VIEW_TYPE_HEADER) {
				return 0;
			}
	        return mPositions[position];
		}
		return 0;
	}

	@Override
	public Object[] getSections() {
		return mHeaders;
	}
	
	@Override
	public Cursor swapCursor(Cursor newCursor) {
		if (newCursor != null) {
			getColumnIndices(newCursor);
			getSectionHeaders(newCursor);
		}
		
		return super.swapCursor(newCursor);
	}
	
	public abstract void getColumnIndices(Cursor cursor);
	
	public void getSectionHeaders(Cursor cursor) {
		if (cursor.moveToFirst()) {
			String previousHeader = null;
			
			List<String> headerList = new ArrayList<String>();
			List<Integer> headerPositionList = new ArrayList<Integer>();
			List<Integer> positionList = new ArrayList<Integer>();
			List<Integer> viewTypeList = new ArrayList<Integer>();
			
			int headerPosition = 0;
			int lastHeaderPosition = 0;
			int count = 0;
			do {
				String header = getHeader(mContext, cursor);
				if (!header.equals(previousHeader)) {
					if (count - lastHeaderPosition >= mMinSectionSize || count == 0) {
						headerList.add(header); // Store the actual header
						headerPositionList.add(count); // Store the position (in the dataset) of the header
						positionList.add(headerPosition);  // Store the position in the set of headers
						viewTypeList.add(HeadersResourceCursorAdapter.VIEW_TYPE_HEADER); // Mark type as header
						lastHeaderPosition = count; // Keep track of last position in dataset of header
						
						headerPosition++;
						count++;						
					} else {
						String newHeader = headerList.get(headerPosition - 1).charAt(0) + "-" 
								+ header.charAt(0);
						headerList.set(headerPosition - 1, newHeader);
					}
					previousHeader = header;
				}
				positionList.add(cursor.getPosition());
				viewTypeList.add(HeadersResourceCursorAdapter.VIEW_TYPE_REGULAR);
				count++;
			} while (cursor.moveToNext());
			
			mHeaders = new String[headerList.size()];
			mHeaderPositions = new int[headerPositionList.size()];
			mPositions = new int[positionList.size()];
			mViewTypes = new int[viewTypeList.size()];
			
			headerList.toArray(mHeaders);
			for (int i = 0; i < mHeaderPositions.length; i++) {
				mHeaderPositions[i] = headerPositionList.get(i);
			}
			for (int i = 0; i < mPositions.length; i++) {
				mPositions[i] = positionList.get(i);
				mViewTypes[i] = viewTypeList.get(i);
			}
		}
	}
	
	public void setMinSectionSize(int size) {
		mMinSectionSize = size;
	}
	
	public int getMinSectionSize() {
		return mMinSectionSize;
	}
	
	public abstract String getHeader(Context context, Cursor cursor);	
	
}
