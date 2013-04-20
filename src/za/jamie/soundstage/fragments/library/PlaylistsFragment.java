package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.PlaylistBrowserActivity;
import za.jamie.soundstage.adapters.PlaylistsAdapter;
import za.jamie.soundstage.cursormanager.CursorDefinitions;
import za.jamie.soundstage.cursormanager.CursorManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

public class PlaylistsFragment extends ListFragment {
	
	public static final String EXTRA_ITEM_ID = "extra_item_id";
	
	//private static final String TAG = "PlaylistsFragment";
	
	private PlaylistsAdapter mAdapter;
	
	public static PlaylistsFragment newInstance(long itemId) {
		final Bundle args = new Bundle();
		args.putLong(EXTRA_ITEM_ID, itemId);
		
		PlaylistsFragment frag = new PlaylistsFragment();
		frag.setArguments(args);
		return frag;
	}
	
	/**
     * Empty constructor as per the Fragment documentation
     */
    public PlaylistsFragment() {}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAdapter = new PlaylistsAdapter(getActivity(), 
        		R.layout.list_item_one_line, null, 0);
        
        setListAdapter(mAdapter);
        
        CursorManager cm = new CursorManager(getActivity(), mAdapter, 
        		CursorDefinitions.getPlaylistsCursorParams());
        getLoaderManager().initLoader(0, null, cm);
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	Cursor cursor = (Cursor) mAdapter.getItem(position);
		
		int playlistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME);
		
		String playlist = cursor.getString(playlistIdx);
		
		Intent i = new Intent(getActivity(), PlaylistBrowserActivity.class);
		i.putExtra(PlaylistBrowserActivity.EXTRA_PLAYLIST_ID, id);
		i.putExtra(PlaylistBrowserActivity.EXTRA_NAME, playlist);
		
		startActivity(i);
    }

    /*@Override
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
	}*/
}
