package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.LibraryAdapter;
import za.jamie.soundstage.musicstore.CursorManager;
import za.jamie.soundstage.musicstore.MusicStore;
import za.jamie.soundstage.pablo.LastfmUris;
import za.jamie.soundstage.pablo.Pablo;
import za.jamie.soundstage.utils.TextUtils;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class AlbumsFragment extends Fragment implements AdapterView.OnItemClickListener {
	
	public static final String EXTRA_ITEM_ID = "extra_item_id";
	
	//private static final String TAG = "AlbumGridFragment";
    
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
        
        gridView.getViewTreeObserver().addOnGlobalLayoutListener(
        		new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				final int numColumns = gridView.getNumColumns();
				if (numColumns > 0) {
					final int columnWidth = gridView.getColumnWidth();
					//mAdapter.setNumColumns(numColumns);
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
	
	private static class AlbumsAdapter extends LibraryAdapter {

		private int mIdColIdx;
		private int mAlbumColIdx;
		private int mArtistColIdx;
		//private int mAlbumArtColIdx;
		
		private int mItemHeight;
		private GridView.LayoutParams mItemLayoutParams;
		
		private final Context mContext;

		public AlbumsAdapter(Context context, int layout, int headerLayout,
				Cursor c, int flags) {
			super(context, layout, headerLayout, c, flags);
			mContext = context;
			mItemLayoutParams = new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}

		@Override
		protected void getColumnIndices(Cursor cursor) {
			mIdColIdx = cursor.getColumnIndex(MediaStore.Audio.Albums._ID);
			mAlbumColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
			mArtistColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST);
			//mAlbumArtColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART);
		}

		@Override
		protected String getSection(Context context, Cursor cursor) {
			return TextUtils.headerFor(cursor.getString(mAlbumColIdx));
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			if (view.getLayoutParams().height != mItemHeight) {
				view.setLayoutParams(mItemLayoutParams);
			}
			
			TextView albumText = (TextView) view.findViewById(R.id.title);
			TextView artistText = (TextView) view.findViewById(R.id.subtitle);
			ImageView albumArtImage = (ImageView) view.findViewById(R.id.image);
			
			String album = cursor.getString(mAlbumColIdx);
			String artist = cursor.getString(mArtistColIdx);
			albumText.setText(album);
			artistText.setText(artist);

			long id = cursor.getLong(mIdColIdx);
			Uri uri = LastfmUris.getAlbumInfoUri(album, artist, id);

			Pablo.with(mContext)
				.load(uri)
				.resize(mItemHeight, mItemHeight)
				.centerCrop()
				.into(albumArtImage);
		}
		
		public void setItemHeight(int height) {
			if (height == mItemHeight) {
				return;
			}
			mItemHeight = height;
			mItemLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
			notifyDataSetChanged();
		}
		
		public int getItemHeight() {
			return mItemHeight;
		}
		
	}

}
