package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.MusicActivity;
import za.jamie.soundstage.adapters.abs.LibraryAdapter;
import za.jamie.soundstage.animation.ViewFlipper;
import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.musicstore.CursorManager;
import za.jamie.soundstage.musicstore.MusicStore;
import za.jamie.soundstage.pablo.LastfmUris;
import za.jamie.soundstage.pablo.Pablo;
import za.jamie.soundstage.service.MusicService;
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
import android.widget.Toast;

public class AlbumsFragment extends Fragment implements AdapterView.OnItemClickListener {
	
	public static final String EXTRA_ITEM_ID = "extra_item_id";
	
	//private static final String TAG = "AlbumGridFragment";
	
	private ViewFlipper mFlipper;
    
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
        
        mFlipper = new ViewFlipper(R.id.grid_item, R.id.flipped_view);
        mAdapter.setFlippedViewListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				MusicItem item = (MusicItem) v.getTag();
				final MusicActivity activity = (MusicActivity) getActivity();
				
				int action = 0; 
				switch(v.getId()) {
				case R.id.flipped_view_now:
					action = MusicService.NOW;
					activity.showPlayer();
					break;
				case R.id.flipped_view_next:
					action = MusicService.NEXT;
					Toast.makeText(activity, "'" + item.title + "' will play next." , Toast.LENGTH_SHORT).show();
					break;
				case R.id.flipped_view_last:
					action = MusicService.LAST;
					Toast.makeText(activity, "'" + item.title + "' will play last." , Toast.LENGTH_SHORT).show();
					break;
				case R.id.flipped_view_more:
					break;
				}

				activity.getMusicConnection().enqueue(item, action);
				
				mFlipper.unflip();
			}
		});
        
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
        gridView.setOnItemLongClickListener(mFlipper);
        gridView.setOnScrollListener(mFlipper);
        
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
	
	private static class AlbumsAdapter extends LibraryAdapter {

		private int mIdColIdx;
		private int mAlbumColIdx;
		private int mArtistColIdx;
		
		private View.OnClickListener mFlippedViewListener;
		
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
		
		public void setFlippedViewListener(View.OnClickListener listener) {
			mFlippedViewListener = listener;
		}

		@Override
		protected void getColumnIndices(Cursor cursor) {
			mIdColIdx = cursor.getColumnIndex(MediaStore.Audio.Albums._ID);
			mAlbumColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
			mArtistColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST);
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
				.fit()
				.centerCrop()
				.into(albumArtImage);
			
			final MusicItem tag = new MusicItem(id, album, MusicItem.TYPE_ALBUM);
			TextView now = (TextView) view.findViewById(R.id.flipped_view_now);
			now.setTag(tag);
			now.setOnClickListener(mFlippedViewListener);
			TextView next = (TextView) view.findViewById(R.id.flipped_view_next);
			next.setTag(tag);
			next.setOnClickListener(mFlippedViewListener);
			TextView last = (TextView) view.findViewById(R.id.flipped_view_last);
			last.setTag(tag);
			last.setOnClickListener(mFlippedViewListener);
			TextView more = (TextView) view.findViewById(R.id.flipped_view_more);
			more.setTag(tag);
			more.setOnClickListener(mFlippedViewListener);
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
