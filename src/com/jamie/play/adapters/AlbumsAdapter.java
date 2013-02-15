package com.jamie.play.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jamie.play.R;
import com.jamie.play.adapters.abs.AlbumAdapter;
import com.jamie.play.bitmapfun.ImageFetcher;

public class AlbumsAdapter extends AlbumAdapter {
	private ImageFetcher mImageWorker;
	
	private int mItemHeight = 0;
    private int mNumColumns = 0;
    private RelativeLayout.LayoutParams mImageViewLayoutParams;    
	
	public AlbumsAdapter(Context context, int layout, Cursor cursor, int flags, 
			ImageFetcher imageWorker) {
		super(context, layout, cursor, flags);
		mImageWorker = imageWorker;
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
            image.setLayoutParams(mImageViewLayoutParams);
        }
        
        final long albumId = cursor.getLong(getIdColIdx());
        
        mImageWorker.loadAlbumImage(albumId, artist, album, image);
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
        mImageViewLayoutParams =
                new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
        notifyDataSetChanged();
    }

    public void setNumColumns(int numColumns) {
        mNumColumns = numColumns;
    }

    public int getNumColumns() {
        return mNumColumns;
    }
    
}
