package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.LibraryAdapter;
import za.jamie.soundstage.adapters.utils.OneTimeDataSetObserver;
import za.jamie.soundstage.musicstore.CursorManager;
import za.jamie.soundstage.musicstore.MusicStore;
import za.jamie.soundstage.utils.TextUtils;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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
        
        final long itemId = getArguments().getLong(EXTRA_ITEM_ID, -1);
        if (itemId > 0) {
        	new OneTimeDataSetObserver(mAdapter) {
				@Override
				public void onFirstChange() {
					gridView.setSelection(mAdapter.getItemPosition(itemId));
				}
        	};
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
		
		private static final Uri ALBUM_ART_BASE_URI = 
				Uri.parse("content://media/external/audio/albumart");
		
		private int mAlbumColIdx;
		private int mAlbumIdColIdx;
		private int mArtistColIdx;
		
		private final Context mContext;

		public AlbumsAdapter(Context context, int layout, int headerLayout,
				Cursor c, int flags) {
			super(context, layout, headerLayout, c, flags);
			mContext = context;
		}

		@Override
		protected void getColumnIndices(Cursor cursor) {
			mAlbumColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
			mAlbumIdColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID);
			mArtistColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST);
		}

		@Override
		protected String getSection(Context context, Cursor cursor) {
			return TextUtils.headerFor(cursor.getString(mAlbumColIdx));
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView album = (TextView) view.findViewById(R.id.title);
			TextView artist = (TextView) view.findViewById(R.id.subtitle);
			ImageView albumArt = (ImageView) view.findViewById(R.id.image);
			
			album.setText(cursor.getString(mAlbumColIdx));
			artist.setText(cursor.getString(mArtistColIdx));
			
			final Uri uri = 
					ContentUris.withAppendedId(ALBUM_ART_BASE_URI, cursor.getLong(mAlbumIdColIdx));
			Picasso.with(mContext).load(uri).into(albumArt);
		}
		
	}

}
