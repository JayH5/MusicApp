package za.jamie.soundstage.adapters;

import java.util.ArrayList;
import java.util.List;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.AlbumAdapter;
import za.jamie.soundstage.bitmapfun.ImageFetcher;
import za.jamie.soundstage.utils.ImageUtils;
import za.jamie.soundstage.utils.TextUtils;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class AlbumsAdapter extends AlbumAdapter implements SectionIndexer {
	
	private static final int MIN_SECTION_SIZE = 5;
	
	private ImageFetcher mImageWorker;
	
	private Character[] mSectionHeaders;
	private Integer[] mSectionPositions;
	private Object[] mGridObjects;
	
	private int mItemHeight = 0;
    private FrameLayout.LayoutParams mSquareLayoutParams;
    
    private final Context mContext;
    private final LayoutInflater mInflater;
    private int mHeaderLayout;
	
	public AlbumsAdapter(Context context, int layout, int headerLayout, Cursor cursor, int flags) {
		super(context, layout, cursor, flags);
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mHeaderLayout = headerLayout;
		mImageWorker = ImageUtils.getThumbImageFetcher(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (mGridObjects == null || position < 0 || position >= mGridObjects.length) {
            return null;
        }
		
		final Object gridObject = mGridObjects[position];
		View v = null;
		
		if (gridObject instanceof String) { // Header
			final String header = (String) gridObject;
			if (convertView == null || convertView.findViewById(R.id.header) == null ) {
				v = newView(mContext, header, parent);
			} else {
				v = convertView;
			}
			bindView(v, mContext, header);
		} else if (gridObject instanceof Integer) { // Cursor position
			final Cursor cursor = getCursor();
			if (convertView == null || convertView.findViewById(R.id.title) == null) {
                v = newView(mContext, cursor, parent);
            } else {
            	v = convertView;
            }            
            
            cursor.moveToPosition((Integer) gridObject);
            bindView(v, mContext, cursor);
		}
		return v;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {		
        final TextView title = (TextView) view.findViewById(R.id.title);
        final TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
        final ImageView image = (ImageView) view.findViewById(R.id.image);
        
        final String album = cursor.getString(getAlbumColIdx());
        final String artist = cursor.getString(getArtistColIdx());
        
        title.setText(album);
        subtitle.setText(artist);
        
        // Check the height matches our calculated column width
        if (image.getLayoutParams().height != mItemHeight) {
            image.setLayoutParams(mSquareLayoutParams);
        }
        
        final long albumId = cursor.getLong(getIdColIdx());
        
        mImageWorker.loadAlbumImage(albumId, artist, album, image);
	}
	
	public void bindView(View view, Context context, String headerText) {
		final TextView headerView = (TextView) view.findViewById(R.id.header);
		headerView.setText(headerText);
		
		if (headerView.getLayoutParams().height != mItemHeight) {
			headerView.setLayoutParams(mSquareLayoutParams);
		}
	}
	
	public View newView(Context context, String header, ViewGroup parent) {
		return mInflater.inflate(mHeaderLayout, parent, false);
	}
	
	/**
     * Sets the item height. Useful for when we know the column width so the height can be set
     * to match.
     *
     * @param height
     */
    public void setItemHeight(int height) {
        if (height == mItemHeight) {
            return;
        }
        mItemHeight = height;
        mSquareLayoutParams =
                new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
        notifyDataSetChanged();
    }
    
    public int getItemHeight() {
    	return mItemHeight;
    }
    
    @Override
    public int getCount() {
    	return (mGridObjects != null) ? mGridObjects.length : 0; 
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
    	if (mGridObjects != null) {
    		Object gridObject = mGridObjects[position];
    		if (gridObject instanceof String) {
    			return (String) gridObject;
    		} else if (gridObject instanceof Integer) {
    			Cursor cursor = getCursor();
    			cursor.moveToPosition((Integer) gridObject);
    			return cursor;
    		}
    	}
    	return null;
    }
    
    @Override
	protected void onCursorLoad(Cursor cursor) {
		super.onCursorLoad(cursor);
		
		if (cursor.moveToFirst()) {
			final int titleColIdx = getAlbumColIdx();
			char startLetter, endLetter;
			startLetter = endLetter = TextUtils.getFirstChar(cursor.getString(titleColIdx));
			
			List<Character> sections = new ArrayList<Character>();
			List<Integer> positions = new ArrayList<Integer>();
			List<Object> objects = new ArrayList<Object>(); // At least
			
			int count = 0;
			int headerPosition = 0;
			
			int numMiscChars = 0;
			int numDigitChars = 0;
			
			// Alphabet (soup) loop
			do {
				final String title = cursor.getString(titleColIdx);
				final char header = TextUtils.getFirstChar(title);
				if (header != endLetter) {
					sections.add(header);
					positions.add(count);
					
					if (Character.isLetter(header)) {
						if (numDigitChars > 0 || numMiscChars > 0) {
							numDigitChars = numMiscChars = 0;
							startLetter = endLetter = header;
							headerPosition = count;
						} else if (count - headerPosition > MIN_SECTION_SIZE) {
							if (endLetter != startLetter) {
								objects.add(headerPosition, startLetter + "-" + endLetter); // teensy hack
							} else {
								objects.add(headerPosition, String.valueOf(endLetter));
							}
							count++;
							headerPosition = count;
							startLetter = endLetter = header;
						} else {
							endLetter = header;
						}
					} else if (Character.isDigit(header)) {
						if (numDigitChars == 0) {
							headerPosition = count;
						} else if (numDigitChars == MIN_SECTION_SIZE) {
							objects.add(headerPosition, "123");
							count++;
						}
						numDigitChars++;
					} else {
						if (numMiscChars == 0) {
							headerPosition = count;
						} else if (numMiscChars == MIN_SECTION_SIZE) {
							objects.add(headerPosition, "!?$");
							count++;
						}
						numMiscChars++;
					}
				}
				objects.add(cursor.getPosition());
				count++;
			} while (cursor.moveToNext());
			// TODO: Add last header
			
			
			mSectionHeaders = new Character[sections.size()];
			sections.toArray(mSectionHeaders);
			
			mSectionPositions = new Integer[positions.size()]; 
			positions.toArray(mSectionPositions);
			
			mGridObjects = objects.toArray();
		}
	}
    
    @Override
    public boolean areAllItemsEnabled() {
    	return false;
    }
    
    @Override
    public boolean isEnabled(int position) {
    	return (mGridObjects != null) ? (mGridObjects[position] instanceof Integer) : false;
    }

	@Override
	public int getPositionForSection(int section) {
		return (mSectionPositions != null) ? (Integer) mSectionPositions[section] : 0;
	}

	@Override
	public int getSectionForPosition(int position) {
		if (mSectionPositions != null) {
            for (int i = 0; i < mSectionPositions.length - 1; i++) {
                if (position >= mSectionPositions[i]
                        && position < mSectionPositions[i + 1]) {
                    return i;
                }
            }
            if (position >= mSectionPositions[mSectionPositions.length - 1]) {
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
