package com.jamie.play.fragments.albumbrowser;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jamie.play.R;
import com.jamie.play.activities.ArtistBrowserActivity;
import com.jamie.play.bitmapfun.ImageFetcher;
import com.jamie.play.cursormanager.CursorManager.CursorLoaderListener;
import com.jamie.play.fragments.ImageDialogFragment;
import com.jamie.play.utils.ImageUtils;
import com.jamie.play.utils.TextUtils;

public class AlbumSummaryFragment extends Fragment implements CursorLoaderListener {
	
	public static final String EXTRA_ALBUM = "extra_album";
	public static final String EXTRA_ARTIST = "extra_artist";
	public static final String EXTRA_ALBUM_ID = "extra_album_id";
	
	private ImageFetcher mImageWorker;
	
	private TextView mNumTracksText;
	private TextView mDurationText;
	private TextView mYearsText;
	
	private ImageView mAlbumArt;
	
	private long mAlbumId;
	private String mAlbum;
	private String mArtist;
	
	private long mArtistId = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments();
		mAlbumId = args.getLong(EXTRA_ALBUM_ID);
		mAlbum = args.getString(EXTRA_ALBUM);
		mArtist = args.getString(EXTRA_ARTIST);
		
		Log.d("AlbumSummary", "Loading summary for albumId: " 
				+ mAlbumId + ", album: " + mAlbum +", artist: " + mArtist);
		
		// Get the image worker... can't load artwork until view inflated
		mImageWorker = ImageUtils.getImageFetcher(getActivity());
	}
	
	@Override
	public View onCreateView(
			LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		final View view = inflater.inflate(R.layout.fragment_album_summary, container, 
				false);
		
		mNumTracksText = (TextView) view.findViewById(R.id.albumTracks);
		mDurationText = (TextView) view.findViewById(R.id.albumDuration);
		mYearsText = (TextView) view.findViewById(R.id.albumYear);
		mAlbumArt = (ImageView) view.findViewById(R.id.albumThumb);
		
		mImageWorker.loadAlbumImage(mAlbumId, mArtist, mAlbum, mAlbumArt);
		mAlbumArt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ImageDialogFragment.showAlbumImage(mAlbumId, mArtist, mAlbum, 
						mImageWorker, getFragmentManager());
				
			}
		});
		
		final ImageButton browseArtistButton = (ImageButton) view
				.findViewById(R.id.browse_artist_button);
		final ImageButton addToQueueButton = (ImageButton) view
				.findViewById(R.id.add_to_queue_button);
		
		browseArtistButton.setOnClickListener(mBrowseArtistListener);		
		addToQueueButton.setOnClickListener(mAddToQueueListener);
		
		return view;
	}
	
	private View.OnClickListener mAddToQueueListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private View.OnClickListener mBrowseArtistListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			final Intent i = new Intent(getActivity(), ArtistBrowserActivity.class);
			i.putExtra(ArtistBrowserActivity.EXTRA_ARTIST_ID, mArtistId);
			i.putExtra(ArtistBrowserActivity.EXTRA_ARTIST, mArtist);
			
			startActivity(i);
		}
	};

	@Override
	public void onLoadFinished(Cursor cursor) {
		if (cursor != null) {			
			// Get the column indexes
			int durationColIdx = 
					cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
			int yearColIdx =
					cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR);
			int artistIdColIdx =
					cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID);
			int artistColIdx =
					cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
			
			int numTracks = cursor.getCount();
			
			long duration = 0;
			int firstYear = Integer.MAX_VALUE;
			int lastYear = 0;
			if (cursor.moveToFirst()) {
				do {
					// Add up the duration
					duration += cursor.getLong(durationColIdx);
					
					// Get the first/last year
					int year = cursor.getInt(yearColIdx);
					firstYear = Math.min(year, firstYear);
					lastYear = Math.max(year, lastYear);
					
					// Maybe there is an easier way of getting the artist id
					if (mArtistId < 0) {
						long artistId = cursor.getLong(artistIdColIdx);
						String artist = cursor.getString(artistColIdx);
						if (artist.equals(mArtist)) {
							mArtistId = artistId;
						}
					}
					
				} while (cursor.moveToNext());
			}
			
			final Resources res = getResources();
			mNumTracksText.setText(TextUtils.getNumTracksText(res, numTracks));
			mDurationText.setText(TextUtils.getStatsDurationText(res, duration));			
			mYearsText.setText(TextUtils.getYearText(res, firstYear, lastYear));
		}
		
	}

	@Override
	public void onLoaderReset() {
		// TODO Auto-generated method stub
		
	}
	
}