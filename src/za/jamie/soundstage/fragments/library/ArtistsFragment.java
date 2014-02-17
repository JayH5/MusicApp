package za.jamie.soundstage.fragments.library;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.library.ArtistsAdapter;
import za.jamie.soundstage.adapters.utils.FlippingViewHelper;
import za.jamie.soundstage.animation.ViewFlipper;
import za.jamie.soundstage.fragments.MusicListFragment;
import za.jamie.soundstage.providers.MusicLoaders;

public class ArtistsFragment extends MusicListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final String ARG_ITEM_ID = "extra_item_id";
	
	private FlippingViewHelper mFlipHelper;
	
	private ArtistsAdapter mAdapter;
	
	public static ArtistsFragment newInstance(long itemId) {
		final Bundle args = new Bundle();
		args.putLong(ARG_ITEM_ID, itemId);
		
		ArtistsFragment frag = new ArtistsFragment();
		frag.setArguments(args);
		return frag;
	}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAdapter = new ArtistsAdapter(getActivity(), 
        		R.layout.list_item_artist, R.layout.list_item_header, null, 0);
        
        setListAdapter(mAdapter);
        
        ViewFlipper flipper = new ViewFlipper(getActivity(), R.id.list_item, R.id.flipped_view);
        mFlipHelper = new FlippingViewHelper(getMusicActivity(), flipper);
        mAdapter.setFlippingViewHelper(mFlipHelper);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, 
    		Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.list_fragment_fastscroll, parent, false);    	
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {		
		Uri data = ContentUris.withAppendedId(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, id);
    	Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(data, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE);
		startActivity(intent);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);
    	mFlipHelper.initFlipper(getListView());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return MusicLoaders.artists(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        if (!mAdapter.isEmpty()) {
            long itemId = getArguments().getLong(ARG_ITEM_ID);
            if (itemId > 0) {
                int itemPosition = mAdapter.getItemPosition(itemId);
                if (itemPosition >= 0 && itemPosition < mAdapter.getCount()) {
                    getListView().setSelection(itemPosition);
                }
                getArguments().remove(ARG_ITEM_ID);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

}
