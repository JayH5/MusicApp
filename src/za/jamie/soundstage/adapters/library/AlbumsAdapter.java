package za.jamie.soundstage.adapters.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.LibraryAdapter;
import za.jamie.soundstage.adapters.utils.FlippingViewHelper;
import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.pablo.LastfmUris;
import za.jamie.soundstage.pablo.Pablo;
import za.jamie.soundstage.utils.TextUtils;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class AlbumsAdapter extends LibraryAdapter {

	private int mIdColIdx;
	private int mAlbumColIdx;
	private int mArtistColIdx;
	
	private FlippingViewHelper mFlipHelper;
	
	private int mItemHeight;
	private GridView.LayoutParams mItemLayoutParams;
	
	private final Context mContext;

	public AlbumsAdapter(Context context, int layout, int headerLayout,
			Cursor c, int flags) {
		super(context, layout, headerLayout, c, flags);
		mContext = context;
		mItemLayoutParams = new GridView.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
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

		Pablo.with(mContext)
			.load(uri)
			.fit()
			.centerCrop()
			.into(albumArtImage);
		
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
	
}
