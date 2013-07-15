package za.jamie.soundstage.musicstore;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;

public class CursorRequest {

	private final Uri mUri;
	private final String[] mProjection;
	private final String mSelection;
	private final String[] mSelectionArgs;
	private final String mSortOrder;

	public CursorRequest(Uri uri, String[] projection, String selection, 
			String[] selectionArgs, String sortOrder) {
		mUri = uri;
		mProjection = projection;
		mSelection = selection;
		mSelectionArgs = selectionArgs;
		mSortOrder = sortOrder;
	}
	
	public CursorLoader createLoader(Context context) {
		return new CursorLoader(context, mUri, mProjection, mSelection, 
				mSelectionArgs, mSortOrder);
	}

}
