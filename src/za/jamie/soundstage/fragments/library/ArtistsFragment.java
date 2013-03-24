package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.ArtistBrowserActivity;
import za.jamie.soundstage.adapters.ArtistsAdapter;
import za.jamie.soundstage.bitmapfun.ImageFetcher;
import za.jamie.soundstage.cursormanager.CursorDefinitions;
import za.jamie.soundstage.cursormanager.CursorManager;
import za.jamie.soundstage.cursormanager.CursorManager.CursorLoaderListener;
import za.jamie.soundstage.utils.ImageUtils;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class ArtistsFragment extends ListFragment implements CursorLoaderListener {
	
	public static final String EXTRA_ITEM_ID = "extra_item_id";
	
	private static final String TAG = "ArtistListFragment";
	
	private ArtistsAdapter mAdapter;
	
	private ImageFetcher mImageWorker;
	
	public static ArtistsFragment newInstance(long itemId) {
		final Bundle args = new Bundle();
		args.putLong(EXTRA_ITEM_ID, itemId);
		
		ArtistsFragment frag = new ArtistsFragment();
		frag.setArguments(args);
		return frag;
	}
	
	/**
     * Empty constructor as per the Fragment documentation
     */
    public ArtistsFragment() {}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mImageWorker = ImageUtils.getImageFetcher(getActivity());
        
        mAdapter = new ArtistsAdapter(getActivity(), 
        		R.layout.list_item_artist, null, 0, mImageWorker);
        
        setListAdapter(mAdapter);
        
        CursorManager cm = new CursorManager(getActivity(), mAdapter, 
        		CursorDefinitions.getArtistsCursorParams(), this);
        getLoaderManager().initLoader(0, null, cm);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);
    	
    	ListView lv = getListView();
    	lv.setFastScrollEnabled(true);
    	lv.setVerticalScrollBarEnabled(false);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mImageWorker.setExitTasksEarly(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageWorker.setExitTasksEarly(true);
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	Cursor cursor = (Cursor) mAdapter.getItem(position);
		
		int artistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);
		
		String artist = cursor.getString(artistIdx);
		
		Log.d(TAG, "Artist selected: " + artist);
		
		Intent i = new Intent(getActivity(), ArtistBrowserActivity.class);
		i.putExtra(ArtistBrowserActivity.EXTRA_ARTIST_ID, id);
		i.putExtra(ArtistBrowserActivity.EXTRA_ARTIST, artist);
		
		startActivity(i);
    }

	@Override
	public void onLoadFinished(Cursor data) {
		long itemId = getArguments().getLong(EXTRA_ITEM_ID, -1);
		if (itemId > 0 && data != null) {
			int idColIdx = data.getColumnIndexOrThrow(BaseColumns._ID);
			
			// Sequential search through the cursor till we find the artist
			if (data.moveToFirst()) {
				int position = 0;
				do {
					if (data.getLong(idColIdx) == itemId) {
						position = data.getPosition();
						break;
					}
				} while (data.moveToNext());
				
				// Then set the first list position to be at that artist
				getListView().setSelection(position);
			}
		}
	}

	@Override
	public void onLoaderReset() {
		// TODO Auto-generated method stub
		
	}
}
