package com.jamie.play.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jamie.play.R;
import com.jamie.play.bitmapfun.ImageWorker;
import com.jamie.play.utils.ImageUtils;

public class ImageDialogFragment extends DialogFragment {
	private static final String EXTRA_IMAGE_KEY = "extra_image_key";
	
	private String mKey;
	
	private ImageWorker mImageWorker;
	
	public static ImageDialogFragment newInstance(String key) {
		final Bundle args = new Bundle();
		args.putString(EXTRA_IMAGE_KEY, key);
		
		final ImageDialogFragment frag = new ImageDialogFragment();
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final Bundle args = getArguments();
		if (args.containsKey(EXTRA_IMAGE_KEY)) {
			mKey = args.getString(EXTRA_IMAGE_KEY);
		} else {
			mKey = savedInstanceState.getString(EXTRA_IMAGE_KEY);
		}
		
		mImageWorker = ImageUtils.getImageFetcher(getActivity());
		
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString(EXTRA_IMAGE_KEY, mKey);
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
	public View onCreateView(
			LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_image_dialog, container,
				false);
		final ImageView imageView = (ImageView) v.findViewById(R.id.imageBig);
		
		mImageWorker.loadImage(mKey, imageView);
			
		
		// Image will be dismissed when touched
		v.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();				
			}
		});
		
		return v;
	}

}
