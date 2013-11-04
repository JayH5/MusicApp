package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.library.PlaylistsAdapter;
import za.jamie.soundstage.fragments.MusicListFragment;
import za.jamie.soundstage.musicstore.CursorManager;
import za.jamie.soundstage.musicstore.MusicStore;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ListView;

public class PlaylistsFragment extends MusicListFragment {
	
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
        		R.layout.list_item_one_line, R.layout.list_item_header, null, 0);
        
        setListAdapter(adapter);
        
        CursorManager cm = new CursorManager(getActivity(), adapter, 
        		MusicStore.Playlists.REQUEST);
        getLoaderManager().initLoader(2, null, cm);
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	Uri data = ContentUris.withAppendedId(
				MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, id);
    	
    	Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(data, MediaStore.Audio.Playlists.ENTRY_CONTENT_TYPE);
		
		startActivity(intent);
    }

}
