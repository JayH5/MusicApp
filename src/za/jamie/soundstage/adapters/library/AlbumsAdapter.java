package za.jamie.soundstage.adapters.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.LibraryAdapter;
import za.jamie.soundstage.adapters.utils.FlippingViewHelper;
import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.pablo.AlbumGridGradient;
import za.jamie.soundstage.pablo.LastfmUris;
import za.jamie.soundstage.pablo.Pablo;
import za.jamie.soundstage.utils.TextUtils;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class AlbumsAdapter extends LibraryAdapter {

	private int mIdColIdx;
	private int mAlbumColIdx;
	private int mArtistColIdx;
	
	private FlippingViewHelper mFlipHelper;
    private AlbumGridGradient mGradient;
	
	private int mNumColumns;
    private int mHeaderHeight;
    private int mItemHeight;
	private GridView.LayoutParams mItemLayoutParams;
	
	private final Context mContext;

	public AlbumsAdapter(Context context, int layout, int headerLayout,
			Cursor c, int flags) {
		super(context, layout, headerLayout, c, flags);
		mContext = context;
		mItemLayoutParams = new GridView.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mHeaderHeight = mContext.getResources().getDimensionPixelSize(R.dimen.spacer_album_grid);
	}
	
	public void setFlippingViewHelper(FlippingViewHelper helper) {
		mFlipHelper = helper;
	}

	@Override
	protected void getColumnIndices(Cursor cursor) {
		mIdColIdx = cursor.getColumnIndex(MediaStore.Audio.Albums._ID);
		mAlbumColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
		mArtistColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST);
	}

	@Override
	protected String getSection(Context context, Cursor cursor) {
		return TextUtils.headerFor(cursor.getString(mAlbumColIdx));
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (view.getLayoutParams().height != mItemHeight) {
			view.setLayoutParams(mItemLayoutParams);
		}
		
		TextView albumText = (TextView) view.findViewById(R.id.title);
		TextView artistText = (TextView) view.findViewById(R.id.subtitle);
		ImageView albumArtImage = (ImageView) view.findViewById(R.id.image);
		
		String album = cursor.getString(mAlbumColIdx);
		String artist = cursor.getString(mArtistColIdx);
		albumText.setText(album);
		artistText.setText(artist);

		long id = cursor.getLong(mIdColIdx);
		Uri uri = LastfmUris.getAlbumInfoUri(album, artist, id);

        if (mItemHeight > 0) {
            if (mGradient == null) {
                mGradient = new AlbumGridGradient(mContext.getResources(), mItemHeight, mItemHeight);
            }

            Pablo.with(mContext)
                    .load(uri)
                    .fit()
                    .centerCrop()
                    .transform(mGradient)
                    .into(albumArtImage);
        }
		
		if (mFlipHelper != null) {
			MusicItem item = new MusicItem(id, album, MusicItem.TYPE_ALBUM);
			mFlipHelper.bindFlippedViewButtons(view, item);
		}
	}
	
	public void setItemHeight(int height) {
		if (height == mItemHeight) {
			return;
		}
		mItemHeight = height;
		mItemLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
		notifyDataSetChanged();
	}
	
	public int getItemHeight() {
		return mItemHeight;
	}

    public void setNumColumns(int numColumns) {
        mNumColumns = numColumns;
    }

    public int getNumColumns() {
        return mNumColumns;
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position < mNumColumns ?
                getViewTypeCount() - 1 : super.getItemViewType(position - mNumColumns);
    }

    @Override
    public int getCount() {
        if (mNumColumns == 0) {
            return 0;
        }

        return super.getCount() + mNumColumns;
    }

    @Override
    public Object getItem(int position) {
        return position < mNumColumns ? null : super.getItem(position - mNumColumns);
    }

    @Override
    public long getItemId(int position) {
        return position < mNumColumns ? 0 : super.getItemId(position - mNumColumns);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (position < mNumColumns) {
            v = convertView != null ? convertView : new View(mContext);
            v.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mHeaderHeight));
        } else {
            v = super.getView(position - mNumColumns, convertView, parent);
        }

        return v;
    }

    @Override
    public int getSectionForPosition(int position) {
        return position < mNumColumns ? 0 : super.getSectionForPosition(position - mNumColumns);
    }

    @Override
    public int getPositionForSection(int section) {
        return section == 0 ? 0 : super.getPositionForSection(section) + mNumColumns;
    }

    @Override
    public boolean isEnabled(int position) {
        return position < mNumColumns ? false : super.isEnabled(position - mNumColumns);
    }
	
}
