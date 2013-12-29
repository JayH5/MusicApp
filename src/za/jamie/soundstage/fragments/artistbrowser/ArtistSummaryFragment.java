package za.jamie.soundstage.fragments.artistbrowser;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.SummaryAdapter;
import za.jamie.soundstage.fragments.ImageDialogFragment;
import za.jamie.soundstage.pablo.LastfmUris;
import za.jamie.soundstage.pablo.Pablo;
import za.jamie.soundstage.utils.TextUtils;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ArtistSummaryFragment extends Fragment implements 
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final String EXTRA_ARTIST_URI = "extra_artist_uri";
	
	private static final String TAG_IMAGE_DIALOG = "tag_image_dialog";
	
	private Uri mArtistUri;
	
	private OnArtistFoundListener mCallback;
	
	private SummaryAdapter mAdapter;
	
	private TextView mNumAlbumsText;
	private TextView mNumTracksText;
	private TextView mDurationText;
	private ImageView mArtistImage;
	
	public static ArtistSummaryFragment newInstance(Uri data) {
		final Bundle args = new Bundle();
		args.putParcelable(EXTRA_ARTIST_URI, data);
		
		final ArtistSummaryFragment frag = new ArtistSummaryFragment();
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = (OnArtistFoundListener) activity;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mCallback = null;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mArtistUri = getArguments().getParcelable(EXTRA_ARTIST_URI);
		
		mAdapter = new ArtistSummaryAdapter(getActivity(), null);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		
		final View view = inflater.inflate(R.layout.fragment_artist_summary, container,
				false);
		
		mNumAlbumsText = (TextView) view.findViewById(R.id.artistAlbums);
		mNumTracksText = (TextView) view.findViewById(R.id.artistTracks);
		mDurationText = (TextView) view.findViewById(R.id.artistDuration);
		mArtistImage = (ImageView) view.findViewById(R.id.artistThumb);
		
		return view;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), 
				mArtistUri, 
				new String[] {
					MediaStore.Audio.Artists._ID,
					MediaStore.Audio.Artists.ARTIST,
					MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
					MediaStore.Audio.Artists.NUMBER_OF_ALBUMS			
				}, null, null, null);
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);		
	}
	
	private class ArtistSummaryAdapter extends SummaryAdapter {

		public ArtistSummaryAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public void loadSummary(Context context, Cursor cursor) {
			if (cursor != null && cursor.moveToFirst()) {
				int numAlbumsColIdx = cursor.getColumnIndexOrThrow(
						MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
				int numTracksColIdx = cursor.getColumnIndexOrThrow(
						MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
				int artistColIdx = cursor.getColumnIndexOrThrow(
						MediaStore.Audio.Artists.ARTIST);
				
				final Resources res = context.getResources();
				mNumAlbumsText.setText(TextUtils.getNumAlbumsText(res, 
						cursor.getInt(numAlbumsColIdx)));
				mNumTracksText.setText(TextUtils.getNumTracksText(res, 
						cursor.getInt(numTracksColIdx)));
				
				final String artist = cursor.getString(artistColIdx);
				final Uri lastfmUri = LastfmUris.getArtistInfoUri(artist);
				Pablo.with(getActivity())
					.load(lastfmUri)
					.resizeDimen(R.dimen.image_thumb_artist, R.dimen.image_thumb_artist)
					.centerCrop()
					.into(mArtistImage);
				
				final Uri lastfmUriBig = lastfmUri.buildUpon()
                        .appendQueryParameter("size", "mega").build();
                mArtistImage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ImageDialogFragment.newInstance(lastfmUriBig)
							.show(getFragmentManager(), TAG_IMAGE_DIALOG);
					}
				});
				
				if (mCallback != null) {
					mCallback.onArtistFound(artist);
				}
			}		
			
		}
	}
	
	public void setDuration(long duration) {
		if (duration > 0) {
			mDurationText.setText(
					TextUtils.getStatsDurationText(getResources(), duration));
		}
	}
	
	public interface OnArtistFoundListener {
		public void onArtistFound(String artist);
	}

}
