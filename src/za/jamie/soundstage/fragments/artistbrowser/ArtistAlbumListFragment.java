package za.jamie.soundstage.fragments.artistbrowser;

import java.util.List;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.AlbumBrowserActivity;
import za.jamie.soundstage.adapters.abs.ResourceArrayAdapter;
import za.jamie.soundstage.bitmapfun.ImageFetcher;
import za.jamie.soundstage.loaders.ArtistAlbumListLoader;
import za.jamie.soundstage.models.ArtistAlbum;
import za.jamie.soundstage.utils.ImageUtils;
import za.jamie.soundstage.utils.TextUtils;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ArtistAlbumListFragment extends ListFragment implements 
		LoaderManager.LoaderCallbacks<List<ArtistAlbum>> {
	
	public static final String EXTRA_ARTIST_ID = "extra_artist_id";
	public static final String EXTRA_ARTIST = "extra_artist";
	
	private long mArtistId;
	private String mArtist;
	
	private ImageFetcher mImageWorker;
	private ArtistAlbumAdapter mAdapter;
	
	public ArtistAlbumListFragment() {}
	
	public static ArtistAlbumListFragment newInstance(long artistId, String artist) {
		Bundle args = new Bundle();
		args.putLong(EXTRA_ARTIST_ID, artistId);
		args.putString(EXTRA_ARTIST, artist);
		
		ArtistAlbumListFragment frag = new ArtistAlbumListFragment();
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        mArtistId = getArguments().getLong(EXTRA_ARTIST_ID);
        mArtist = getArguments().getString(EXTRA_ARTIST);
        
        mImageWorker = ImageUtils.getImageFetcher(getActivity());
        
        /*mAdapter = new ArtistAlbumsAdapter(getActivity(), 
        		R.layout.list_item_artist_album, null, 0);*/
        
        mAdapter = new ArtistAlbumAdapter(getActivity(), 
        		R.layout.list_item_artist_album, null);
        
        setListAdapter(mAdapter);
        
        /*CursorManager cm = new CursorManager(getActivity(), mAdapter, 
        		CursorDefinitions.getArtistAlbumsCursorParams(mArtist));
        getLoaderManager().initLoader(1, null, cm);*/
        
        getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mImageWorker.setExitTasksEarly(false);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mImageWorker.setExitTasksEarly(true);
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		ArtistAlbum artistAlbum = (ArtistAlbum) mAdapter.getItem(position);
		
		final Intent i = new Intent(getActivity(), AlbumBrowserActivity.class);		
		i.putExtra(AlbumBrowserActivity.EXTRA_ALBUM_ID, id);
		i.putExtra(AlbumBrowserActivity.EXTRA_ALBUM, artistAlbum.album);
		i.putExtra(AlbumBrowserActivity.EXTRA_ARTIST, mArtist);
		
		startActivity(i);
	}
		
	private class ArtistAlbumAdapter extends ResourceArrayAdapter<ArtistAlbum> {

		public ArtistAlbumAdapter(Context context, int resource,
				List<ArtistAlbum> objects) {
			super(context, resource, objects);
		}

		@Override
		public void bindView(View view, Context context, ArtistAlbum artistAlbum) {
			final ImageView albumArt = (ImageView) view.findViewById(R.id.albumThumb);
			final TextView titleText = (TextView) view.findViewById(R.id.albumName);
			final TextView yearText = (TextView) view.findViewById(R.id.albumYear);
			final TextView numTracksText = (TextView) view.findViewById(R.id.albumTracks);
			
			titleText.setText(artistAlbum.album);
			mImageWorker.loadAlbumImage(artistAlbum.albumId, albumArt);
			
			final Resources res = getResources();
			numTracksText.setText(TextUtils.getNumTracksText(res, artistAlbum.numTracks));
			yearText.setText(TextUtils.getYearText(res, artistAlbum.firstYear, 
					artistAlbum.lastYear));
		}
		
		@Override
		public long getItemId(int position) {
			ArtistAlbum artistAlbum = (ArtistAlbum) getItem(position);
			if (artistAlbum != null) {
				return artistAlbum.albumId;
			}
			return -1;
		}

		
	}
	
	@Override
	public Loader<List<ArtistAlbum>> onCreateLoader(int id, Bundle args) {
		return new ArtistAlbumListLoader(getActivity(), mArtistId);
	}

	@Override
	public void onLoadFinished(Loader<List<ArtistAlbum>> loader, List<ArtistAlbum> data) {
		mAdapter.setList(data);	
	}

	@Override
	public void onLoaderReset(Loader<List<ArtistAlbum>> arg0) {
		mAdapter.setList(null);	
	}

}
