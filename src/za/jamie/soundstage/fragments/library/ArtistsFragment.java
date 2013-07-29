package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.LibraryAdapter;
import za.jamie.soundstage.adapters.utils.OneTimeDataSetObserver;
import za.jamie.soundstage.bitmapfun.ImageFetcher;
import za.jamie.soundstage.fragments.MusicListFragment;
import za.jamie.soundstage.musicstore.CursorManager;
import za.jamie.soundstage.musicstore.MusicStore;
import za.jamie.soundstage.utils.ImageUtils;
import za.jamie.soundstage.utils.TextUtils;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ArtistsFragment extends MusicListFragment {
	
	public static final String EXTRA_ITEM_ID = "extra_item_id";
	
	public static ArtistsFragment newInstance(long itemId) {
		final Bundle args = new Bundle();
		args.putLong(EXTRA_ITEM_ID, itemId);
		
		ArtistsFragment frag = new ArtistsFragment();
		frag.setArguments(args);
		return frag;
	}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final ArtistsAdapter adapter = new ArtistsAdapter(getActivity(), 
        		R.layout.list_item_artist, R.layout.list_item_header, null, 0);
        
        setListAdapter(adapter);
        
        final long itemId = getArguments().getLong(EXTRA_ITEM_ID, -1);
        if (itemId > 0) {
	        new OneTimeDataSetObserver(adapter) {
				@Override
				public void onFirstChange() {
					setSelection(adapter.getItemPosition(itemId));
				}
	        };
        }
        
        CursorManager cm = new CursorManager(getActivity(), adapter, 
        		MusicStore.Artists.REQUEST);
        getLoaderManager().initLoader(0, null, cm);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, 
    		Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.list_fragment_fastscroll, parent, false);    	
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {		
		Uri data = ContentUris.withAppendedId(
				MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, id);
    	
    	Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(data, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE);

		startActivity(intent);
    }
    
    private static class ArtistsAdapter extends LibraryAdapter {

    	private int mArtistColIdx;
    	private int mArtistIdColIdx;
    	private int mNumAlbumsIdx;
    	private int mNumTracksIdx;
    	
    	private ImageFetcher mImageWorker;
    	
    	public ArtistsAdapter(Context context, int layout, int headerLayout,
				Cursor c, int flags) {
			super(context, layout, headerLayout, c, flags);
			mImageWorker = ImageUtils.getThumbImageFetcher(context);
		}

		@Override
		protected void getColumnIndices(Cursor cursor) {
			mArtistColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);
			mArtistIdColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID);
			mNumAlbumsIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
			mNumTracksIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
		}

		@Override
		protected String getSection(Context context, Cursor cursor) {
			return TextUtils.headerFor(cursor.getString(mArtistColIdx));
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView title = (TextView) view.findViewById(R.id.artistName);
			TextView numAlbumsText = (TextView) view.findViewById(R.id.artistAlbums);
			TextView numTracksText = (TextView) view.findViewById(R.id.artistTracks);
			ImageView artistImage = (ImageView) view.findViewById(R.id.artistImage);
			
			String artist = cursor.getString(mArtistColIdx);
			title.setText(artist);
			
			final Resources res = context.getResources();
			numAlbumsText.setText(TextUtils.getNumAlbumsText(res, cursor.getInt(mNumAlbumsIdx)));
			numTracksText.setText(TextUtils.getNumTracksText(res, cursor.getInt(mNumTracksIdx)));
			
			mImageWorker.loadArtistImage(cursor.getLong(mArtistIdColIdx), artist, artistImage);
		}
    	
    }

}
