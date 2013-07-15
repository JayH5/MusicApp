package za.jamie.soundstage.musicstore;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.CursorAdapter;

public class CursorManager implements LoaderManager.LoaderCallbacks<Cursor> {

	private final Context mContext;
	private final CursorAdapter mAdapter;
	private final CursorRequest mRequest;
	
	public CursorManager(Context context, CursorAdapter adapter, 
			CursorRequest request) {
		mContext = context;
		mAdapter = adapter;
		mRequest = request;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return mRequest.createLoader(mContext);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    	mAdapter.swapCursor(data); // Swap new cursor in
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
}