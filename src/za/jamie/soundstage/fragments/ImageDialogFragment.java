package za.jamie.soundstage.fragments;

import java.lang.ref.WeakReference;

import za.jamie.soundstage.R;
import za.jamie.soundstage.pablo.Pablo;
import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Callback;

public class ImageDialogFragment extends DialogFragment {
	private static final String EXTRA_IMAGE_KEY = "extra_image_key";
	
	private Uri mUri;
	
	public static ImageDialogFragment newInstance(Uri key) {
		final Bundle args = new Bundle();
		args.putParcelable(EXTRA_IMAGE_KEY, key);
		
		final ImageDialogFragment frag = new ImageDialogFragment();
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUri = getArguments().getParcelable(EXTRA_IMAGE_KEY);
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
	}
	
	@Override
	public View onCreateView(
			LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_image_dialog, container, false);
		
		final ImageView imageView = (ImageView) v.findViewById(R.id.imageBig);
		int size = getResources().getDisplayMetrics().widthPixels;
		Pablo.with(getActivity())
			.load(mUri)
			.resize(size, size)
			.centerInside()
			.into(imageView, new ImageCallback(v));			
		
		// Image will be dismissed when touched
		v.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				dismiss();				
			}
		});
		
		return v;
	}
	
	private static class ImageCallback implements Callback {

		private final WeakReference<View> mView;
		
		public ImageCallback(View dialogView) {
			mView = new WeakReference<View>(dialogView);
		}
		
		@Override
		public void onError() {
			View view = mView.get();
			if (view != null) {
				view.findViewById(R.id.progress_spinner).setVisibility(View.GONE);
				view.findViewById(R.id.empty).setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onSuccess() {
			View view = mView.get();
			if (view != null) {
				view.findViewById(R.id.progress_spinner).setVisibility(View.GONE);
			}
		}
		
	}

}
