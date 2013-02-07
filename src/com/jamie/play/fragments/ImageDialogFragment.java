package com.jamie.play.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jamie.play.R;
import com.jamie.play.R.id;
import com.jamie.play.R.layout;
import com.jamie.play.bitmapfun.ImageFetcher;

public class ImageDialogFragment extends DialogFragment {
	enum ImageType {
		Artist,
		Album
	}
	
	private long mId;
	private String mArtist;
	private String mAlbum;
	private ImageType mImageType;
	
	private ImageFetcher mImageWorker;
	
	public static void showArtistImage(long artistId, String artist, 
			ImageFetcher imageFetcher, FragmentManager fm) {
		
		new ImageDialogFragment()
				.setImageFetcher(imageFetcher)
				.setArtistInfo(artistId, artist)
				.show(fm, null);
	}
	
	public static void showAlbumImage(long albumId, String artist,
			String album, ImageFetcher imageFetcher, FragmentManager fm) {
		
		new ImageDialogFragment()
				.setImageFetcher(imageFetcher)
				.setAlbumInfo(albumId, artist, album)
				.show(fm, null);
	}
	
	public ImageDialogFragment setImageFetcher(ImageFetcher imageFetcher) {
		mImageWorker = imageFetcher;
		return this;
	}
	
	public ImageDialogFragment setArtistInfo(long artistId, String artist) {
		mId = artistId;
		mArtist = artist;
		mImageType = ImageType.Artist;
		return this;
	}
	
	public ImageDialogFragment setAlbumInfo(long albumId, String artist, 
			String album) {
		mId = albumId;
		mArtist = artist;
		mAlbum = album;
		mImageType = ImageType.Album;
		return this;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_image_dialog, container, false);
		ImageView image = (ImageView) v.findViewById(R.id.imageBig);
		
		switch(mImageType) {
		case Artist:
			mImageWorker.loadArtistImage(mId, mArtist, image);
			break;
		case Album:
			mImageWorker.loadAlbumImage(mId, mArtist, mAlbum, image);
		}
			
		
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
