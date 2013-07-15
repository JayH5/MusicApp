package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.PlaylistBrowserActivity;
import za.jamie.soundstage.adapters.PlaylistsAdapter;
import za.jamie.soundstage.musicstore.CursorManager;
import za.jamie.soundstage.musicstore.MusicStore;
import android.app.ListFragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ListView;

public class PlaylistsFragment extends ListFragment {
	
	public static final String EXTRA_ITEM_ID = "extra_item_id";
	
	public static PlaylistsFragment newInstance(long itemId) {
		final Bundle args = new Bundle();
		args.putLong(EXTRA_ITEM_ID, itemId);
		
		PlaylistsFragment frag = new PlaylistsFragment();
		frag.setArguments(args);
		return frag;
	}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        PlaylistsAdapter adapter = new PlaylistsAdapter(getActivity(), 
        		R.layout.list_item_one_line, null, 0);
        
        setListAdapter(adapter);
        
        CursorManager cm = new CursorManager(getActivity(), adapter, 
        		MusicStore.Playlists.REQUEST);
        getLoaderManager().initLoader(0, null, cm);
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	Cursor cursor = (Cursor) getListAdapter().getItem(position);
		
		int playlistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME);
		
		String playlist = cursor.getString(playlistIdx);
		
		Intent i = new Intent(getActivity(), PlaylistBrowserActivity.class);
		i.putExtra(PlaylistBrowserActivity.EXTRA_PLAYLIST_ID, id);
		i.putExtra(PlaylistBrowserActivity.EXTRA_NAME, playlist);
		
		startActivity(i);
    }

}
