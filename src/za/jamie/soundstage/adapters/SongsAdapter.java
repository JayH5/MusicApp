package za.jamie.soundstage.adapters;

import java.util.ArrayList;
import java.util.List;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.TrackAdapter;
import za.jamie.soundstage.utils.TextUtils;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SectionIndexer;
import android.widget.TextView;


public class SongsAdapter extends TrackAdapter implements SectionIndexer {

	private Object[] mSectionHeaders;
	private Object[] mSectionPositions;
	
	public SongsAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
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
			List<String> sections = new ArrayList<String>();
			List<Integer> positions = new ArrayList<Integer>();
			final int titleColIdx = getTitleColIdx();
			//int count = 0;
			do {
				final String title = cursor.getString(titleColIdx);
				final String header = TextUtils.headerFor(title);
				if (!header.equals(previousHeader)) {
					sections.add(header);
					positions.add(cursor.getPosition());					
					
					previousHeader = header;
					// TODO: Add headers
				}
				//count++;
			} while (cursor.moveToNext());
			
			mSectionHeaders = sections.toArray();
			mSectionPositions = positions.toArray();
		}
        
        
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
	
}