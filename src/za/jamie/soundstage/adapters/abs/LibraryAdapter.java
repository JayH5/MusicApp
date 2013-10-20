package za.jamie.soundstage.adapters.abs;

import java.util.ArrayList;
import java.util.List;

import za.jamie.soundstage.adapters.interfaces.SearchableAdapter;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

public abstract class LibraryAdapter extends CursorAdapter implements SectionIndexer, 
		SearchableAdapter {

	private static final int VIEW_TYPE_NORMAL = 0;
	private static final int VIEW_TYPE_HEADER = 1;
	
	// Indexing
	private final List<String> mSections = new ArrayList<String>();
	private final List<Integer> mSectionPositions = new ArrayList<Integer>();
	private Object[] mSectionsArray;
	private int mMinSectionSize = 1;
	
	// This is where the magic happens... For each item in the adapter an integer is
	// stored in this list. This integer is either the cursor position for the item
	// or the position of the section in the list of sections. To differentiate section
	// headers from regular items, section positions are stored as ((-section pos) - 1).
	// All regular items have an "item position" >= 0.
	private final List<Integer> mItemPositions = new ArrayList<Integer>();
	
	// View inflation/binding
	private final Context mContext;
	private final LayoutInflater mInflater;
	private int mLayout;
	private int mHeaderLayout;

	public LibraryAdapter(Context context, int layout, int headerLayout,
			Cursor c, int flags) {
		super(context, c, flags);
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		
		mLayout = layout;
		mHeaderLayout = headerLayout;
	}
	
	@Override
	public Cursor swapCursor(Cursor newCursor) {
		if (newCursor != null) {
			getColumnIndices(newCursor);
			index(newCursor);
		} else {
			clearIndexing();
		}
		return super.swapCursor(newCursor);
	}
	
	protected abstract void getColumnIndices(Cursor cursor);
	
	/**
	 * Bind the header text to a header view. By default the view is assumed to
	 * be a TextView and has the text set in it to "header". To use a more complex
	 * view for a header, override this method and perform binding manually.
	 * @param view
	 * @param context
	 * @param header
	 */
	public void bindHeaderView(View view, Context context, String header) {
		((TextView) view).setText(header);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return mInflater.inflate(mLayout, parent, false);
	}
	
	/**
	 * Makes a new view to hold the text in the header string.
	 * @param context
	 * @param header
	 * @param parent
	 * @return
	 */
	public View newHeaderView(Context context, String header, ViewGroup parent) {
		return mInflater.inflate(mHeaderLayout, parent, false);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int itemPosition = mItemPositions.get(position);
		if (itemPosition < 0) {
			String header = mSections.get(-itemPosition - 1);
			if (convertView == null) {
				convertView = newHeaderView(mContext, header, parent);
			}
			bindHeaderView(convertView, mContext, header);
			return convertView;
		}
		
		return super.getView(itemPosition, convertView, parent);
	}
	
	@Override
	public long getItemId(int position) {
		int itemPosition = mItemPositions.get(position);
		return itemPosition >= 0 ? super.getItemId(itemPosition) : 0;
	}
	
	@Override
	public Object getItem(int position) {
		int itemPosition = mItemPositions.get(position);
		return itemPosition >= 0 ? super.getItem(itemPosition) : mSections.get(-itemPosition - 1);
	}

	@Override
	public int getPositionForSection(int section) {
		return mSectionPositions.get(section);
	}

	@Override
	public int getSectionForPosition(int position) {
		while (position >= 0 && mItemPositions.get(position) >= 0) {
			position--;
		}
		return (-mItemPositions.get(position) - 1);
	}

	@Override
	public Object[] getSections() {
		if (mSectionsArray == null) {
			mSectionsArray = mSections.toArray();
		}
		return mSectionsArray;
	}
	
	@Override
    public boolean areAllItemsEnabled() {
    	return false;
    }
	
	@Override
	public boolean isEnabled(int position) {
		return mItemPositions.get(position) >= 0;
	}
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	@Override
	public int getItemViewType(int position) {
		return mItemPositions.get(position) >= 0 ? VIEW_TYPE_NORMAL : VIEW_TYPE_HEADER;
	}
	
	@Override
	public int getCount() {
		return mItemPositions.size();
	}
	
	private void clearIndexing() {
		mSections.clear();
		mSectionPositions.clear();
		mItemPositions.clear();
		mSectionsArray = null;
	}
	
	/**
	 * Get the section that the cursor's current row belongs in.
	 * @param context
	 * @param cursor The cursor in the correct position
	 * @return The title of the section for the current row in the cursor. This should
	 *  generally be between 1 and 3 characters
	 */
	protected abstract String getSection(Context context, Cursor cursor);
	
	/**
	 * Set the minimum number of items between each header. Default value is 1. Sections
	 * with fewer items than this minimum will be joined with the previous section.
	 * @param size minimum value of 1
	 */
	public void setMinSectionSize(int size) {
		if (size < 1) {
			throw new IllegalArgumentException("Section size must be at least 1 item!");
		}
		mMinSectionSize = size;
		notifyDataSetChanged();
	}
	
	/**
	 * @return the minimum number of items between each header. Default value is 1.
	 */
	public int getMinSectionSize() {
		return mMinSectionSize;
	}
	
	/**
	 * @param position The effective position of the item in the AdapterView
	 * @return The position of the item in the cursor (or a negative number if
	 *  the item is a header
	 */
	public int getCursorPosition(int position) {
		return mItemPositions.get(position);
	}
	
	@Override
	public int getItemPosition(long itemId) {
		for (int i = 0, count = getCount(); i < count; i++) {
			if (getItemId(i) == itemId) {
				return i;
			}
		}
		return 0;
	}
	
	private void index(Cursor cursor) {
		clearIndexing();
		if (cursor.moveToFirst()) {
			int position = 0;
			int section = 0;
			int lastSectionPosition = 0;
			String previousSection = null;
			do {
				final String currentSection = getSection(mContext, cursor);
				if (!currentSection.equals(previousSection)) {
					if (position - lastSectionPosition >= mMinSectionSize || position == 0) {
						// Add the new section
						mSections.add(currentSection);
						// Add its position to the list of positions
						mSectionPositions.add(position);
						// Also store that position for later comparison
						lastSectionPosition = position;
						// Increment the position to account for the header view
						position++;
						// Mark as a header
						mItemPositions.add((-section) - 1);
						// Increment section count
						section++;
					} else {
						// Otherwise, update the previous section so that it spans the new one
						int lastSection = section - 1;
						String newSection = mSections.get(lastSection).charAt(0) + "-" 
								+ currentSection.charAt(0);
						mSections.set(lastSection, newSection);
					}
					// Update previous section record
					previousSection = currentSection;
				}
				mItemPositions.add(cursor.getPosition());
				position++;
			} while (cursor.moveToNext());			
		}
	}

}
