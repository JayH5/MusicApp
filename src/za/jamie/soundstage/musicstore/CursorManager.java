package za.jamie.soundstage.musicstore;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;

public class CursorManager implements LoaderManager.LoaderCallbacks<Cursor> {
	//private static final String TAG = "CursorManager";
	
	private final Context mContext;
	private final CursorAdapter mAdapter;
	private final CursorRequest mCursorParams;
	
	public CursorManager(Context context, CursorAdapter adapter, 
			CursorRequest params) {
		mContext = context;
		mAdapter = adapter;
		mCursorParams = params;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return mCursorParams.load(mContext);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    	mAdapter.swapCursor(data); // Swap new cursor in
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
	
	public static class CursorRequest {
		private Uri mBaseUri;
		private String[] mProjection;
		private String mSelection;
		private String[] mSelectionArgs;
		private String mSortOrder;
		
		public CursorRequest(Uri baseUri) {
			mBaseUri = baseUri;
		}
		
		public CursorRequest(Uri baseUri, String[] projection, String selection, 
				String[] selectionArgs, String sortOrder) {
			mBaseUri = baseUri;
			mProjection = projection;
			mSelection = selection;
			mSelectionArgs = selectionArgs;
			mSortOrder = sortOrder;
		}
		
		public CursorLoader load(Context context) {
			return new CursorLoader(context,
					mBaseUri,
					mProjection,
					mSelection,
					mSelectionArgs,
					mSortOrder);
		}
		
		public CursorRequest setProjection(String[] projection) {
			mProjection = projection;
			return this;
		}
		
		public CursorRequest addProjection(String... projection) {
			if (mProjection != null) {
				final int oldLength = mProjection.length;
				final int extraLength = mProjection.length;
				String[] newProjection = new String[oldLength + extraLength];
				for (int i = 0; i < oldLength; i++) {
					newProjection[i] = mProjection[i];
				}
				for (int i = 0; i < extraLength; i++) {
					newProjection[oldLength + i] = projection[i];
				}
				mProjection = newProjection;
			} else {
				mProjection = projection;
			}
			return this;
		}

		public CursorRequest setSelection(String selection) {
			mSelection = selection;
			return this;
		}
		
		public CursorRequest setSelectionArgs(String[] selectionArgs) {
			mSelectionArgs = selectionArgs;
			return this;
		}
		
		public CursorRequest setSortOrder(String sortOrder) {
			mSortOrder = sortOrder;
			return this;
		}
	}
}