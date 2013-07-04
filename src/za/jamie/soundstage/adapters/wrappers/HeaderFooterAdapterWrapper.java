package za.jamie.soundstage.adapters.wrappers;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

public class HeaderFooterAdapterWrapper extends BaseAdapter {
	
	private ListAdapter mDelegate;
	
	private LayoutInflater mInflater;
	
	private int mHeaderViewResource;
	private int mFooterViewResource;
	protected int mNumHeaders = 0;
	protected int mNumFooters = 0;
	
	protected int mCount = 0;
	
	public HeaderFooterAdapterWrapper(Context context, ListAdapter delegate) {
		init(context, delegate, 0, 0);
	}
	
	public HeaderFooterAdapterWrapper(Context context, ListAdapter delegate, 
			int headerLayout) {
		
		init(context, delegate, headerLayout, 0);
	}
	
	public HeaderFooterAdapterWrapper(Context context, ListAdapter delegate, 
			int headerLayout, int footerLayout) {
		
		init(context, delegate, headerLayout, footerLayout);
	}
	
	private final DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			notifyDataSetChanged();
		}
		
		@Override
		public void onInvalidated() {
			notifyDataSetInvalidated();
		}
	};
	
	private void init(Context context, ListAdapter delegate, int headerLayout, 
			int footerLayout) {
		
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDelegate = delegate;
		mDelegate.registerDataSetObserver(mDataSetObserver);
		mHeaderViewResource = headerLayout;
		mFooterViewResource = footerLayout;
		registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				mCount = getCount();
			}
		});
	}
	
	public void setNumHeaders(int numHeaders) {
		mNumHeaders = numHeaders;
		notifyDataSetChanged();
	}
	
	public int getNumHeaders() {
		return mNumHeaders;	
	}
	
	public void setNumFooters(int numFooters) {
		mNumFooters = numFooters;
		notifyDataSetChanged();
	}
	
	public int getNumFooters() {
		return mNumFooters;
	}
	
	public void setHeaderViewResource(int headerLayout) {
		mHeaderViewResource = headerLayout;
	}
	
	public void setFooterViewResource(int footerLayout) {
		mFooterViewResource = footerLayout;
	}

	@Override
	public int getCount() {
		return mDelegate.getCount() + mNumHeaders + mNumFooters;
	}

	@Override
	public Object getItem(int position) {
		if (position < mNumHeaders || position >= mCount - mNumFooters) {
			return null;
		}
		return mDelegate.getItem(position - mNumHeaders);
	}

	@Override
	public long getItemId(int position) {
		if (position < mNumHeaders || position >= mCount - mNumFooters) {
			return 0;
		}
		return mDelegate.getItemId(position - mNumHeaders);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v;
		if (position < mNumHeaders) {
			v = convertView != null ? convertView :
				mInflater.inflate(mHeaderViewResource, parent, false);
		} else if (position >= mCount - mNumFooters) {
			v = convertView != null ? convertView :
				mInflater.inflate(mFooterViewResource, parent, false);
		} else {
			v = mDelegate.getView(position - mNumHeaders, convertView, parent);
		}
		return v;
	}
	
	@Override
	public int getViewTypeCount() {
		return mDelegate.getViewTypeCount() + 2;
	}
	
	@Override
	public int getItemViewType(int position) {
		int headerTypes = 0;
		if (mNumHeaders > 0) {
			headerTypes++;
			if (mNumFooters > 0 && mHeaderViewResource != mFooterViewResource) {
				headerTypes++;
			}
		}
		
		int itemViewType;
		if (position < mNumHeaders) {
			itemViewType = 0;
		} else if (position >= mCount - mNumFooters) {
			itemViewType = headerTypes > 1 ? 1 : 0;
		} else {
			itemViewType = mDelegate.getItemViewType(position - mNumHeaders) + headerTypes;
		}
		
		return itemViewType;
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return (mNumHeaders == 0 && mNumFooters == 0) && mDelegate.areAllItemsEnabled();
	}
	
	@Override
	public boolean isEnabled(int position) {
		if (position < mNumHeaders || position >= mCount - mNumFooters) {
			return false;
		}
		return mDelegate.isEnabled(position - mNumHeaders);
	}

}
