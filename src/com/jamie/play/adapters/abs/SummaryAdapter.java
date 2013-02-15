package com.jamie.play.adapters.abs;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObservable;
import android.database.DataSetObserver;

public abstract class SummaryAdapter {
	
	private final DataSetObservable mDataSetObservable = new DataSetObservable();
	
	private Cursor mCursor;
	private final Context mContext;
	
    /**
     * Recommended constructor.
     *
     * @param c The cursor from which to get the data.
     * @param context The context
     * @param flags Flags used to determine the behavior of the adapter; may
     * be {@link #FLAG_REGISTER_CONTENT_OBSERVER}.
     */
    public SummaryAdapter(Context context, Cursor c) {
        mContext = context;
    	mCursor = c;
    	
    	registerDataSetObserver(new MyDataSetObserver());
    }
	
	/**
     * Returns the cursor.
     * @return the cursor.
     */
    public Cursor getCursor() {
        return mCursor;
    }
	
	/**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     * 
     * @param cursor The new cursor to be used
     */
    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }
	
	/**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     *
     * @param newCursor The new cursor to be used.
     * @return Returns the previously set Cursor, or null if there wasa not one.
     * If the given new Cursor is the same instance is the previously set
     * Cursor, null is also returned.
     */
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if (newCursor != null) {
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
            // notify the observers about the lack of a data set
            notifyDataSetInvalidated();
        }
        return oldCursor;
    }
    
    /**
     * Notifies the attached observers that the underlying data has been changed
     * and any View reflecting the data set should refresh itself.
     */
    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

    /**
     * Notifies the attached observers that the underlying data is no longer valid
     * or available. Once invoked this adapter is no longer valid and should
     * not report further data set changes.
     */
    public void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }
    
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }
    
    public abstract void loadSummary(Context context, Cursor cursor);
    
    private class MyDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            loadSummary(mContext, getCursor());
        }
    }
}
