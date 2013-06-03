package za.jamie.soundstage.bitmapfun;

import android.graphics.Bitmap;

public interface Cache {
	Bitmap get(String key);
	void put(String key, Bitmap value);
	void remove(String key);
	
	void clear();
	void close();	
}
