package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.MusicActivity;
import za.jamie.soundstage.adapters.FlippingViewHelper;
import za.jamie.soundstage.adapters.library.AlbumsAdapter;
import za.jamie.soundstage.animation.ViewFlipper;
import za.jamie.soundstage.musicstore.CursorManager;
import za.jamie.soundstage.musicstore.MusicStore;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.ContentUris;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;

public class AlbumsFragment extends Fragment implements AdapterView.OnItemClickListener {
	
	public static final String EXTRA_ITEM_ID = "extra_item_id";
	
	//private static final String TAG = "AlbumGridFragment";
	
	private FlippingViewHelper mFlipHelper;
    
    private AlbumsAdapter mAdapter;
    
    public static AlbumsFragment newInstance(long albumId) {
    	final Bundle args = new Bundle();
    	args.putLong(EXTRA_ITEM_ID, albumId);
    	
    	AlbumsFragment frag = new AlbumsFragment();
    	frag.setArguments(args);
    	return frag;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set up the adapter to create the views
        mAdapter = new AlbumsAdapter(getActivity(), 
        		R.layout.grid_item_two_line, R.layout.grid_item_header, null, 0);
        mAdapter.setMinSectionSize(5);
        
        ViewFlipper flipper = new ViewFlipper(R.id.grid_item, R.id.flipped_view);
        mFlipHelper = new FlippingViewHelper((MusicActivity) getActivity(), flipper); 
        mAdapter.setFlippingViewHelper(mFlipHelper);
        
        // Load up the cursor
        final CursorManager cm = new CursorManager(getActivity(), mAdapter, 
        		MusicStore.Albums.REQUEST);
        getLoaderManager().initLoader(0, null, cm);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
    		Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_album_grid, container, false);
        final GridView gridView = (GridView) v.findViewById(R.id.grid);
        
        gridView.setAdapter(mAdapter);        
        gridView.setOnItemClickListener(this);
        mFlipHelper.initFlipper(gridView);
        
        gridView.getViewTreeObserver().addOnGlobalLayoutListener(
        		new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				final int numColumns = gridView.getNumColumns();
				if (numColumns > 0) {
					final int columnWidth = gridView.getColumnWidth();
					mAdapter.setItemHeight(columnWidth);
					gridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}					
			}        			
		});
        
        final long itemId = getArguments().getLong(EXTRA_ITEM_ID, 0);
        if (itemId > 0) {
        	mAdapter.registerDataSetObserver(new DataSetObserver() {
        		@Override
        		public void onChanged() {
        			gridView.setSelection(mAdapter.getItemPosition(itemId));
        			mAdapter.unregisterDataSetObserver(this);
        		}
        	});
        }
        
        return v;
    }
    
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		final Uri data = ContentUris.withAppendedId(
				MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id);
		final Intent intent = new Intent(Intent.ACTION_VIEW)
			.setDataAndType(data, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE);
		ActivityOptions options = 
				ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
		getActivity().startActivity(intent, options.toBundle());
	}

}
