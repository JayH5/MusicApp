package za.jamie.soundstage.bitmapfun;

import android.graphics.Bitmap;

public class SingleBitmapCache implements Cache {

	private String mKey;
	private Bitmap mValue;
	
	public SingleBitmapCache() {
		
	}

	@Override
	public void put(String key, Bitmap value) {
		if (!key.equals(mKey)) {
			mKey = key;
			mValue = value;
		}
	}

	@Override
	public void remove(String key) {
		if (key.equals(mKey)) {
			mValue = null;
		}		
	}

	@Override
	public Bitmap get(String key) {
		if (key.equals(mKey)) {
			return mValue;
		}
		return null;
	}
	
	public Bitmap get() {
		return mValue;
	}

	@Override
	public void clear() {
		mKey = null;
		mValue = null;
	}

	@Override
	public void close() {
		clear();
	}

}
