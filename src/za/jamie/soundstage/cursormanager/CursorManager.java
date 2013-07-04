package za.jamie.soundstage.cursormanager;

import java.util.LinkedList;
import java.util.List;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.CursorAdapter;

public class CursorManager implements LoaderManager.LoaderCallbacks<Cursor> {
	//private static final String TAG = "CursorManager";
	
	private final Context mContext;
	private final CursorAdapter mAdapter;
	private final CursorParams mCursorParams;	
	
	public CursorManager(Context context, CursorAdapter adapter, 
			CursorParams params) {
		mContext = context;
		mAdapter = adapter;
		mCursorParams = params;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return mCursorParams.getCursorLoader(mContext);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    	mAdapter.swapCursor(data); // Swap new cursor in
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
	
	public static class CursorParams {
		private Uri mBaseUri;
		private List<String> mProjection = new LinkedList<String>();
		private String mSelection;
		private String[] mSelectionArgs;
		private String mSortOrder;
		
		public CursorParams(Uri baseUri) {
			mBaseUri = baseUri;
		}
		
		public Uri getUriForId(long id) {
			return Uri.withAppendedPath(mBaseUri, String.valueOf(id));
		}
		
		public Intent constructViewIntent(long id) {
			final Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(getUriForId(id));
			
			return intent;
		}
		
		public CursorLoader getCursorLoader(Context context) {
			return new CursorLoader(context,
					mBaseUri,
					mProjection.toArray(new String[mProjection.size()]),
					mSelection,
					mSelectionArgs,
					mSortOrder);
		}
		
		public CursorParams setProjection(String[] projection) {
			mProjection.clear();
			return addProjection(projection);
		}
		
		public CursorParams addProjection(String[] projection) {
			for (String column : projection) {
				mProjection.add(column);
			}
			return this;
		}
		
		public CursorParams addProjection(String projection) {
			mProjection.add(projection);
			return this;
		}
		
		public CursorParams setSelection(String selection) {
			mSelection = selection;
			return this;
		}
		
		public CursorParams setSelectionArgs(String[] selectionArgs) {
			mSelectionArgs = selectionArgs;
			return this;
		}
		
		public CursorParams setSortOrder(String sortOrder) {
			mSortOrder = sortOrder;
			return this;
		}
		
	}
}