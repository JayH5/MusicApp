package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.ArtistsAdapter;
import za.jamie.soundstage.adapters.abs.ArtistAdapter;
import za.jamie.soundstage.bitmapfun.ImageFetcher;
import za.jamie.soundstage.cursormanager.CursorDefinitions;
import za.jamie.soundstage.cursormanager.CursorManager;
import za.jamie.soundstage.utils.ImageUtils;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

public class ArtistsFragment extends ListFragment {
	
	public static final String EXTRA_ITEM_ID = "extra_item_id";
	
	//private static final String TAG = "ArtistListFragment";
	
	private ArtistAdapter mAdapter;
	
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
    public void onListItemClick(ListView l, View v, int position, long id) {		
		Uri data = ContentUris.withAppendedId(
				MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, id);
    	
    	Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(data, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE);

		startActivity(intent);
    }
}
