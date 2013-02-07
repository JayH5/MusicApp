package com.jamie.play.fragments.library;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.jamie.play.R;
import com.jamie.play.activities.ArtistBrowserActivity;
import com.jamie.play.adapters.ArtistsAdapter;
import com.jamie.play.bitmapfun.ImageFetcher;
import com.jamie.play.cursormanager.CursorDefinitions;
import com.jamie.play.cursormanager.CursorManager;
import com.jamie.play.utils.ImageUtils;

public class ArtistsFragment extends ListFragment {
	private static final String TAG = "ArtistListFragment";
	
	private ArtistsAdapter mAdapter;
	
	private ImageFetcher mImageWorker;
	
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
        		CursorDefinitions.getArtistsCursorParams());
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
		
		int artistIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID);
		int artistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);
		
		long artistId = cursor.getLong(artistIdIdx);
		String artist = cursor.getString(artistIdx);
		
		Log.d(TAG, "Artist selected: " + artist);
		
		Intent i = new Intent(getActivity(), ArtistBrowserActivity.class);
		i.putExtra(ArtistBrowserActivity.EXTRA_ARTIST_ID, artistId);
		i.putExtra(ArtistBrowserActivity.EXTRA_ARTIST, artist);
		
		startActivity(i);
    }
}
