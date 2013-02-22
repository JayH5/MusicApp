package za.jamie.soundstage.cursormanager;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.CursorAdapter;

public class CursorManager implements LoaderManager.LoaderCallbacks<Cursor> {
	//private static final String TAG = "CursorManager";
	
	private final Context mContext;
	private final CursorAdapter mAdapter;
	private final CursorParams mCursorParams;
	
	private CursorLoaderListener mListener;
	
	
	public CursorManager(Context context, CursorAdapter adapter, 
			CursorParams params) {
		mContext = context;
		mAdapter = adapter;
		mCursorParams = params;
	}
	
	public CursorManager(Context context, CursorAdapter adapter, 
			CursorParams params, CursorLoaderListener listener) {
		mContext = context;
		mAdapter = adapter;
		mCursorParams = params;
		mListener = listener;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return mCursorParams.getCursorLoader(mContext);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    	if (mAdapter != null) {
    		mAdapter.swapCursor(data); // Swap new cursor in
    	}
    	if (mListener != null) {
			mListener.onLoadFinished(data);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (mAdapter != null) {
			mAdapter.swapCursor(null);
		}
		if (mListener != null) {
			mListener.onLoaderReset();
		}
	}
	
	public CursorLoaderListener getListener() {
		return mListener;
	}
	
	public void setListener(CursorLoaderListener listener) {
		mListener = listener;
	}
	
	public interface CursorLoaderListener {
		public void onLoadFinished(Cursor data);
		
		public void onLoaderReset();
	}
	
	public static class CursorParams {
		private Uri BASE_URI;
		private String[] PROJECTION;
		private String SELECTION;
		private String[] SELECTION_ARGS;
		private String SORT_ORDER;
		
		public Uri getUriForId(long id) {
			return Uri.withAppendedPath(BASE_URI, String.valueOf(id));
		}
		
		public Intent constructViewIntent(long id) {
			final Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(getUriForId(id));
			
			return intent;
		}
		
		public CursorLoader getCursorLoader(Context context) {
			return new CursorLoader(context,
					BASE_URI,
					PROJECTION,
					SELECTION,
					SELECTION_ARGS,
					SORT_ORDER);
		}
		
		public CursorParams setBaseUri(Uri baseUri) {
			BASE_URI = baseUri;
			return this;
		}
		
		public CursorParams setProjection(String[] projection) {
			PROJECTION = projection;
			return this;
		}
		
		public CursorParams setSelection(String selection) {
			SELECTION = selection;
			return this;
		}
		
		public CursorParams setSelectionArgs(String[] selectionArgs) {
			SELECTION_ARGS = selectionArgs;
			return this;
		}
		
		public CursorParams setSortOrder(String sortOrder) {
			SORT_ORDER = sortOrder;
			return this;
		}
		
	}
}