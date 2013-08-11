package za.jamie.soundstage.utils;

import za.jamie.soundstage.R;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class ViewUtils {

	public static View createListSpacer(Context context) {
		View spacer = new View(context);
    	int height = context.getResources()
    			.getDimensionPixelSize(R.dimen.list_header_footer_spacing);
    	ViewGroup.LayoutParams params = 
    			new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, height);
    	spacer.setLayoutParams(params);
    	return spacer;
	}

}
