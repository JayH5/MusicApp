package za.jamie.soundstage.adapters;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.HeadersResourceCursorAdapter;
import za.jamie.soundstage.bitmapfun.ImageFetcher;
import za.jamie.soundstage.utils.ImageUtils;
import za.jamie.soundstage.utils.TextUtils;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class AlbumsAdapter extends HeadersResourceCursorAdapter {

	private int mAlbumColIdx;
	private int mAlbumIdColIdx;
	private int mArtistColIdx;
	
	private int mItemHeight = 0;
    private FrameLayout.LayoutParams mSquareLayoutParams;
	
	private ImageFetcher mImageWorker;
	
	public AlbumsAdapter(Context context, int layout, int headerLayout,
			Cursor c, int flags) {
		super(context, layout, headerLayout, c, flags);
		mImageWorker = ImageUtils.getThumbImageFetcher(context);
	}

	@Override
	public void bindHeaderView(View view, Context context, String header) {
		TextView headerView = (TextView) view.findViewById(R.id.header);
		headerView.setText(header);
		
		if (headerView.getLayoutParams().height != mItemHeight) {
			headerView.setLayoutParams(mSquareLayoutParams);
		}
	}

	@Override
	public void getColumnIndices(Cursor cursor) {
		mAlbumColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
		mAlbumIdColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID);
		mArtistColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView album = (TextView) view.findViewById(R.id.title);
		TextView artist = (TextView) view.findViewById(R.id.subtitle);
		ImageView albumArt = (ImageView) view.findViewById(R.id.image);
		
		album.setText(cursor.getString(mAlbumColIdx));
		artist.setText(cursor.getString(mArtistColIdx));
		mImageWorker.loadAlbumImage(cursor.getLong(mAlbumIdColIdx), albumArt);
		
		// Check the height matches our calculated column width
        if (albumArt.getLayoutParams().height != mItemHeight) {
        	albumArt.setLayoutParams(mSquareLayoutParams);
        }
	}
	
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
	public String getHeader(Context context, Cursor cursor) {
		return TextUtils.headerFor(cursor.getString(mAlbumColIdx));
	}

}
