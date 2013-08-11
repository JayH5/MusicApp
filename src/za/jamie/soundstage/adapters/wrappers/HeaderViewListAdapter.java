/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.jamie.soundstage.adapters.wrappers;

import java.util.ArrayList;
import java.util.List;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

public class HeaderViewListAdapter implements WrapperListAdapter, Filterable {

    private final ListAdapter mAdapter;

    private final List<FixedViewInfo> mHeaderViewInfos = new ArrayList<FixedViewInfo>();
    private final List<FixedViewInfo> mFooterViewInfos = new ArrayList<FixedViewInfo>();
    
    private final DataSetObservable mObservable = new DataSetObservable();

    private boolean mAreAllFixedViewsSelectable;

    private final boolean mIsFilterable;

    public HeaderViewListAdapter(ListAdapter adapter) {
        mAdapter = adapter;
        mIsFilterable = adapter instanceof Filterable;

        mAreAllFixedViewsSelectable =
                areAllListInfosSelectable(mHeaderViewInfos)
                && areAllListInfosSelectable(mFooterViewInfos);
    }

    public int getHeadersCount() {
        return mHeaderViewInfos.size();
    }

    public int getFootersCount() {
        return mFooterViewInfos.size();
    }

    @Override
    public boolean isEmpty() {
        return mAdapter == null || mAdapter.isEmpty();
    }

    private boolean areAllListInfosSelectable(List<FixedViewInfo> infos) {
        if (infos != null) {
            for (FixedViewInfo info : infos) {
                if (!info.isSelectable) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Add a fixed view to appear at the top of the list. If addHeaderView is
     * called more than once, the views will appear in the order they were
     * added. Views added using this call can take focus if they want.
     * <p>
     * NOTE: Call this before calling setAdapter. This is so ListView can wrap
     * the supplied cursor with one that will also account for header and footer
     * views.
     *
     * @param v The view to add.
     * @param data Data to associate with this view
     * @param isSelectable whether the item is selectable
     */
    public void addHeaderView(View v, Object data, boolean isSelectable) {
        FixedViewInfo info = new FixedViewInfo();
        info.view = v;
        info.data = data;
        info.isSelectable = isSelectable;
        mHeaderViewInfos.add(info);
        
        mObservable.notifyChanged();
    }
    
    /**
     * Add a fixed view to appear at the top of the list. If addHeaderView is
     * called more than once, the views will appear in the order they were
     * added. Views added using this call can take focus if they want.
     * <p>
     * NOTE: Call this before calling setAdapter. This is so ListView can wrap
     * the supplied cursor with one that will also account for header and footer
     * views.
     *
     * @param v The view to add.
     */
    public void addHeaderView(View v) {
        addHeaderView(v, null, true);
    }
    
    /**
     * Add a fixed view to appear at the bottom of the list. If addFooterView is
     * called more than once, the views will appear in the order they were
     * added. Views added using this call can take focus if they want.
     * <p>
     * NOTE: Call this before calling setAdapter. This is so ListView can wrap
     * the supplied cursor with one that will also account for header and footer
     * views.
     *
     * @param v The view to add.
     * @param data Data to associate with this view
     * @param isSelectable true if the footer view can be selected
     */
    public void addFooterView(View v, Object data, boolean isSelectable) {
        FixedViewInfo info = new FixedViewInfo();
        info.view = v;
        info.data = data;
        info.isSelectable = isSelectable;
        mFooterViewInfos.add(info);
        
        mObservable.notifyChanged();
    }
    
    /**
     * Add a fixed view to appear at the bottom of the list. If addFooterView is called more
     * than once, the views will appear in the order they were added. Views added using
     * this call can take focus if they want.
     * <p>NOTE: Call this before calling setAdapter. This is so ListView can wrap the supplied
     * cursor with one that will also account for header and footer views.
     *
     *
     * @param v The view to add.
     */
    public void addFooterView(View v) {
        addFooterView(v, null, true);
    }

    public void removeHeader(View v) {
        for (int i = 0; i < mHeaderViewInfos.size(); i++) {
            FixedViewInfo info = mHeaderViewInfos.get(i);
            if (info.view == v) {
                mHeaderViewInfos.remove(i);

                mAreAllFixedViewsSelectable =
                        areAllListInfosSelectable(mHeaderViewInfos)
                        && areAllListInfosSelectable(mFooterViewInfos);

                mObservable.notifyChanged();
                
                break;
            }
        }
    }

    public void removeFooter(View v) {
        for (int i = 0; i < mFooterViewInfos.size(); i++) {
            FixedViewInfo info = mFooterViewInfos.get(i);
            if (info.view == v) {
                mFooterViewInfos.remove(i);

                mAreAllFixedViewsSelectable =
                        areAllListInfosSelectable(mHeaderViewInfos)
                        && areAllListInfosSelectable(mFooterViewInfos);

                mObservable.notifyChanged();
                
                break;
            }
        }
    }

    @Override
    public int getCount() {
        if (mAdapter != null) {
            return getFootersCount() + getHeadersCount() + mAdapter.getCount();
        } else {
            return getFootersCount() + getHeadersCount();
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        if (mAdapter != null) {
            return mAreAllFixedViewsSelectable && mAdapter.areAllItemsEnabled();
        } else {
            return true;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        // Header (negative positions will throw an ArrayIndexOutOfBoundsException)
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return mHeaderViewInfos.get(position).isSelectable;
        }

        // Adapter
        final int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (mAdapter != null) {
            adapterCount = mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return mAdapter.isEnabled(adjPosition);
            }
        }

        // Footer (off-limits positions will throw an ArrayIndexOutOfBoundsException)
        return mFooterViewInfos.get(adjPosition - adapterCount).isSelectable;
    }

    @Override
    public Object getItem(int position) {
        // Header (negative positions will throw an ArrayIndexOutOfBoundsException)
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return mHeaderViewInfos.get(position).data;
        }

        // Adapter
        final int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (mAdapter != null) {
            adapterCount = mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getItem(adjPosition);
            }
        }

        // Footer (off-limits positions will throw an ArrayIndexOutOfBoundsException)
        return mFooterViewInfos.get(adjPosition - adapterCount).data;
    }

    @Override
    public long getItemId(int position) {
        int numHeaders = getHeadersCount();
        if (mAdapter != null && position >= numHeaders) {
            int adjPosition = position - numHeaders;
            int adapterCount = mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getItemId(adjPosition);
            }
        }
        return -1;
    }

    @Override
    public boolean hasStableIds() {
        if (mAdapter != null) {
            return mAdapter.hasStableIds();
        }
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Header (negative positions will throw an ArrayIndexOutOfBoundsException)
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return mHeaderViewInfos.get(position).view;
        }

        // Adapter
        final int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (mAdapter != null) {
            adapterCount = mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getView(adjPosition, convertView, parent);
            }
        }

        // Footer (off-limits positions will throw an ArrayIndexOutOfBoundsException)
        return mFooterViewInfos.get(adjPosition - adapterCount).view;
    }

    @Override
    public int getItemViewType(int position) {
        int numHeaders = getHeadersCount();
        if (mAdapter != null && position >= numHeaders) {
            int adjPosition = position - numHeaders;
            int adapterCount = mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getItemViewType(adjPosition);
            }
        }

        return AdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER;
    }

    @Override
    public int getViewTypeCount() {
        if (mAdapter != null) {
            return mAdapter.getViewTypeCount();
        }
        return 1;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
    	mObservable.registerObserver(observer);
        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(observer);
        }
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
    	mObservable.unregisterObserver(observer);
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(observer);
        }
    }

    @Override
    public Filter getFilter() {
        if (mIsFilterable) {
            return ((Filterable) mAdapter).getFilter();
        }
        return null;
    }
 
    @Override
    public ListAdapter getWrappedAdapter() {
        return mAdapter;
    }
    
    /**
     * A class that represents a fixed view in a list, for example a header at the top
     * or a footer at the bottom.
     */
    private class FixedViewInfo {
        /** The view to add to the list */
        public View view;
        /** The data backing the view. This is returned from {@link ListAdapter#getItem(int)}. */
        public Object data;
        /** <code>true</code> if the fixed view should be selectable in the list */
        public boolean isSelectable;
    }

}