package za.jamie.soundstage.adapters.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.LibraryAdapter;
import za.jamie.soundstage.adapters.utils.FlippingViewHelper;
import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.utils.TextUtils;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

public class PlaylistsAdapter extends LibraryAdapter {

	private int mIdColIdx;
	private int mNameColIdx;
	
	private FlippingViewHelper mFlipHelper;
	
	public PlaylistsAdapter(Context context, int layout, int headerLayout, Cursor c, int flags) {
		super(context, layout, headerLayout, c, flags);
	}
	
	public void setFlippingViewHelper(FlippingViewHelper helper) {
		mFlipHelper = helper;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView nameText = (TextView) view.findViewById(R.id.title);
		String name = cursor.getString(mNameColIdx);
		nameText.setText(name);
		
		if (mFlipHelper != null) {
			MusicItem item =
					new MusicItem(cursor.getLong(mIdColIdx), name, MusicItem.TYPE_PLAYLIST);
			mFlipHelper.bindFlippedViewButtons(view, item);
		}
	}
	
	@Override
	protected void getColumnIndices(Cursor cursor) {
		mIdColIdx = cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Playlists._ID);
		mNameColIdx = cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME);
	}
	
	public int getIdColIdx() {
		return mIdColIdx;
	}

	@Override
	protected String getSection(Context context, Cursor cursor) {
		return TextUtils.headerFor(cursor.getString(mNameColIdx));
	}

}
