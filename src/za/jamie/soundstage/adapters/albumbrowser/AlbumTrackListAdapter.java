package za.jamie.soundstage.adapters.albumbrowser;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.BasicTrackAdapter;
import za.jamie.soundstage.adapters.utils.FlippingViewHelper;
import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.utils.TextUtils;

public class AlbumTrackListAdapter extends BasicTrackAdapter {

	private static final int VIEW_TYPE_TRACK = 0;
    private static final int VIEW_TYPE_HEADER = 1;

    private int mTrackNumColIdx;
	private boolean mIsCompilation = false;

    private ArrayList<Integer> mItems;
	
	private FlippingViewHelper mFlipHelper;

    private final Context mContext;
    private final LayoutInflater mInflater;
    private int mHeaderLayout;
	
	public AlbumTrackListAdapter(Context context, int layout, int headerLayout, Cursor c, int flags) {
		super(context, layout, c, flags);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mHeaderLayout = headerLayout;
        mItems = new ArrayList<Integer>();
	}
	
	public void setFlippingViewHelper(FlippingViewHelper helper) {
		mFlipHelper = helper;
	}

	@Override
	protected void getColumnIndices(Cursor cursor) {
		super.getColumnIndices(cursor);
		mTrackNumColIdx = cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK);
		
		checkCompilationAndIndexDiscs(cursor);
	}

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == null) {
            mItems.clear();
        }
        return super.swapCursor(newCursor);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int itemPosition = mItems.get(position);
        if (itemPosition < 0) {
            View header = convertView != null ?
                    convertView : mInflater.inflate(mHeaderLayout, parent, false);

            int discNumber = -(itemPosition + 1);
            ((TextView) header).setText(mContext.getString(R.string.disc_number, discNumber));
            return header;
        }

        return super.getView(itemPosition, convertView, parent);
    }

    /**
     * Scan through a new cursor to check if the album is a compilation and to note locations of
     * disc start/end positions so that headers can be placed.
     * @param cursor
     */
    private void checkCompilationAndIndexDiscs(Cursor cursor) {
        mItems.clear();
        mItems.ensureCapacity(cursor.getCount());
        if (cursor.moveToFirst()) {
            // Get the initial artistId and disc number
            final long artistId = cursor.getLong(getArtistIdColIdx());
            int discNumber = cursor.getInt(mTrackNumColIdx) / 1000;

            // Ignore difference between disc 1 and disc 0, likely bad metadata
            discNumber = discNumber > 0 ? discNumber : 1;

            // If not multi-disc then don't show any headers
            boolean isMultiDisc = false;
            mItems.add(0);
            while (cursor.moveToNext()) {
                if (artistId != cursor.getLong(getArtistIdColIdx())) {
                    mIsCompilation = true;
                }
                int newDiscNumber = cursor.getInt(mTrackNumColIdx) / 1000;
                if (newDiscNumber > discNumber) {
                    int headerItem = -newDiscNumber - 1;
                    if (!isMultiDisc) {
                        mItems.add(0, -discNumber - 1);
                        isMultiDisc = true;
                    }
                    mItems.add(headerItem);
                    discNumber = newDiscNumber;
                }
                mItems.add(cursor.getPosition());
            }
        }
    }

    /**
     * Get the cursor position for the position in the adapter
     * @param position a position in the adapter
     * @return the cursor position or a number < 0 if the position does not correspond to a position
     *  in the cursor.
     */
    public int getCursorPosition(int position) {
        return mItems.get(position);
    }
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {			
		TextView titleText = (TextView) view.findViewById(R.id.title);
		TextView subtitleText = (TextView) view.findViewById(R.id.subtitle);
		TextView trackNumText = (TextView) view.findViewById(R.id.trackNumber);
		
		String title = cursor.getString(getTitleColIdx());
		titleText.setText(title);
		
		if (mIsCompilation) {
			subtitleText.setText(cursor.getString(getArtistColIdx()));
		} else {
			long duration = cursor.getLong(getDurationColIdx());
			subtitleText.setText(TextUtils.getTrackDurationText(duration));
		}
		
		int trackNum = cursor.getInt(mTrackNumColIdx);
		trackNumText.setText(TextUtils.getTrackNumText(trackNum));
		
		if (mFlipHelper != null) {
			MusicItem item = new MusicItem(cursor.getLong(getIdColIdx()), title, MusicItem.TYPE_TRACK);
			mFlipHelper.bindFlippedViewButtons(view, item);
		}
	}

    @Override
    public long getItemId(int position) {
        int itemPosition = mItems.get(position);
        return itemPosition >= 0 ? super.getItemId(itemPosition) : 0;
    }

    @Override
    public Object getItem(int position) {
        int itemPosition = mItems.get(position);
        return itemPosition >= 0 ? super.getItem(itemPosition) : null;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position) >= 0 ? VIEW_TYPE_TRACK : VIEW_TYPE_HEADER;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return mItems.get(position) >= 0;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }
	
}
