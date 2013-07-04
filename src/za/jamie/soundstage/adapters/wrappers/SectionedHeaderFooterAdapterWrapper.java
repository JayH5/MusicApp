package za.jamie.soundstage.adapters.wrappers;

import android.content.Context;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SectionIndexer;

public class SectionedHeaderFooterAdapterWrapper extends
		HeaderFooterAdapterWrapper implements SectionIndexer {

	private SectionIndexer mDelegate;
	
	public SectionedHeaderFooterAdapterWrapper(Context context,
			ListAdapter delegate) {
		
		super(context, delegate);
		init(delegate);
	}

	public SectionedHeaderFooterAdapterWrapper(Context context,
			ListAdapter delegate, int headerLayout) {
		
		super(context, delegate, headerLayout);
		init(delegate);
	}

	public SectionedHeaderFooterAdapterWrapper(Context context,
			ListAdapter delegate, int headerLayout, int footerLayout) {
		
		super(context, delegate, headerLayout, footerLayout);
		init(delegate);
	}
	
	private void init(ListAdapter delegate) {
		try {
			mDelegate = (SectionIndexer) delegate;
		} catch (ClassCastException e) {
			throw new ClassCastException("Adapter must implement SectionIndexer!");
		}
	}

	@Override
	public int getPositionForSection(int position) {
		if (position < mNumHeaders) {
			position = mNumHeaders;
		} else if (position >= mCount - mNumFooters) {
			position = mCount - mNumFooters - 1;
		}
		return mDelegate.getPositionForSection(position - mNumHeaders);
	}

	@Override
	public int getSectionForPosition(int position) {
		if (position < mNumHeaders) {
			position = mNumHeaders;
		} else if (position >= mCount - mNumFooters) {
			position = mCount - mNumFooters - 1;
		}
		return mDelegate.getSectionForPosition(position - mNumHeaders);
	}

	@Override
	public Object[] getSections() {
		Log.d("Adapter", "Sections: " + mDelegate.getSections().length);
		return mDelegate.getSections();
	}

}
