package za.jamie.soundstage.adapters.utils;

import android.database.DataSetObserver;
import android.widget.Adapter;

public abstract class OneTimeDataSetObserver extends DataSetObserver {

	private final Adapter mAdapter;
	
	public OneTimeDataSetObserver(Adapter adapter) {
		mAdapter = adapter;
		mAdapter.registerDataSetObserver(this);
	}
	
	@Override
	public final void onChanged() {
		onFirstChange();
		mAdapter.unregisterDataSetObserver(this);
	}
	
	public abstract void onFirstChange();

}
