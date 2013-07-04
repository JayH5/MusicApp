package za.jamie.soundstage.fragments.artistbrowser;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.ArtistAlbumListAdapter;
import za.jamie.soundstage.adapters.wrappers.HeaderFooterAdapterWrapper;
import za.jamie.soundstage.cursormanager.CursorDefinitions;
import za.jamie.soundstage.cursormanager.CursorManager;
import za.jamie.soundstage.fragments.DefaultListFragment;
import android.content.ContentUris;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.widget.ListView;

public class ArtistAlbumListFragment extends DefaultListFragment {
	
	public static final String EXTRA_ARTIST_ID = "extra_artist_id";
	
	public ArtistAlbumListFragment() {}
	
	public static ArtistAlbumListFragment newInstance(long artistId) {
		Bundle args = new Bundle();
		args.putLong(EXTRA_ARTIST_ID, artistId);
		
		ArtistAlbumListFragment frag = new ArtistAlbumListFragment();
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        CursorAdapter adapter = new ArtistAlbumListAdapter(getActivity(), 
        		R.layout.list_item_artist_album, null, 0);
        
        HeaderFooterAdapterWrapper wrapper = new HeaderFooterAdapterWrapper(getActivity(), adapter);
        
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	wrapper.setHeaderViewResource(R.layout.list_item_spacer);
        	wrapper.setFooterViewResource(R.layout.list_item_spacer);
        	wrapper.setNumHeaders(1);
        	wrapper.setNumFooters(1);
        } else {
        	wrapper.setHeaderViewResource(R.layout.list_item_spacer_vpi);
        	wrapper.setNumHeaders(1);
        }
        
        setListAdapter(wrapper);
        
        long artistId = getArguments().getLong(EXTRA_ARTIST_ID);
        CursorManager cm = new CursorManager(getActivity(), adapter, 
        		CursorDefinitions.getArtistAlbumsCursorParams(artistId));
        
        getLoaderManager().initLoader(1, null, cm);
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {		
		final Uri data = ContentUris.withAppendedId(
				MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id);
		final Intent intent = new Intent(Intent.ACTION_VIEW)		
			.setDataAndType(data, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE);
		
		startActivity(intent);
	}

}
