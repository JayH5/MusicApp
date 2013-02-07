package com.jamie.play.fragments.artistbrowser;

import java.util.Set;
import java.util.TreeSet;

import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jamie.play.R;
import com.jamie.play.bitmapfun.ImageFetcher;
import com.jamie.play.cursormanager.CursorManager.CursorLoaderListener;
import com.jamie.play.fragments.ImageDialogFragment;
import com.jamie.play.utils.ImageUtils;
import com.jamie.play.utils.TextUtils;

public class ArtistSummaryFragment extends Fragment implements CursorLoaderListener {

	public static final String EXTRA_ARTIST = "extra_artist";
	public static final String EXTRA_ARTIST_ID = "extra_artist_id";
	
	private long mArtistId;
	private String mArtist;
	
	private TextView mNumAlbumsText;
	private TextView mNumTracksText;
	private TextView mDurationText;
	private ImageView mArtistImage;
	
	private ImageFetcher mImageWorker;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments();
		mArtistId = args.getLong(EXTRA_ARTIST_ID);
		mArtist = args.getString(EXTRA_ARTIST);
		
		// Get the image worker... can't load artwork until view inflated
		mImageWorker = ImageUtils.getImageFetcher(getActivity());
	}
	
	@Override
	public View onCreateView(
			LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		final View view = inflater.inflate(R.layout.fragment_artist_summary, container,
				false);
		
		mNumAlbumsText = (TextView) view.findViewById(R.id.artistAlbums);
		mNumTracksText = (TextView) view.findViewById(R.id.artistTracks);
		mDurationText = (TextView) view.findViewById(R.id.artistDuration);
		mArtistImage = (ImageView) view.findViewById(R.id.artistThumb);
		
		mImageWorker.loadArtistImage(mArtistId, mArtist, mArtistImage);
		mArtistImage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ImageDialogFragment.showArtistImage(mArtistId, mArtist, mImageWorker, 
						getFragmentManager());
			}
		});
		
		return view;
	}
	
	@Override
	public void onLoadFinished(Cursor cursor) {
		if (cursor != null) {
			// Get column indices
			int albumIdColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
			
			int durationColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
			
			int numTracks = cursor.getCount();
			
			// Count distinct albums, add up total duration
			int numAlbums = 0;
			long duration = 0;
			if (cursor.moveToFirst()) {
				Set<Long> albumIds = new TreeSet<Long>();
				do {
					albumIds.add(cursor.getLong(albumIdColIdx));
					duration += cursor.getLong(durationColIdx);
				} while (cursor.moveToNext());
				numAlbums = albumIds.size();
			}
			
			final Resources res = getResources();
			mNumAlbumsText.setText(TextUtils.getNumAlbumsText(res, numAlbums));
			mNumTracksText.setText(TextUtils.getNumTracksText(res, numTracks));
			mDurationText.setText(TextUtils.getStatsDurationText(res, duration));
		}		
	}

	@Override
	public void onLoaderReset() {
		// TODO Auto-generated method stub
		
	}

}
