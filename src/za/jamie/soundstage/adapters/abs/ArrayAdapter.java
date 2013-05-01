/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2013 Jamie Hewland
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

package za.jamie.soundstage.adapters.abs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

public abstract class ArrayAdapter<T> extends BaseAdapter implements Filterable {

	/**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private List<T> mObjects;

    /**
     * Lock used to modify the content of {@link #mObjects}. Any write operation
     * performed on the array should be synchronized on this lock. This lock is also
     * used by the filter (see {@link #getFilter()} to make a synchronized copy of
     * the original array of data.
     */
    private final Object mLock = new Object();
    
    private Context mContext;
    

    // A copy of the original mObjects array, initialized from and then used instead as soon as
    // the mFilter ArrayFilter is used. mObjects will then only contain the filtered values.
    private ArrayList<T> mOriginalValues;
    private ArrayFilter mFilter;
    
    public ArrayAdapter(Context context, List<T> array) {
    	mContext = context;
    	if (array != null) {
    		mObjects = array;
    	} else {
    		mObjects = new ArrayList<T>();
    	}    	
    }
    
    public ArrayAdapter(Context context, T[] array) {
    	mContext = context;
    	mObjects = new ArrayList<T>();
    	if (array != null) {
    		Collections.addAll(mObjects, array);
    	}
    }
    
    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     */
    public void add(T object) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(object);
            } else {
                mObjects.add(object);
            }
        }
        notifyDataSetChanged();
    }
    
    /**
     * Adds the specified Collection at the end of the array.
     *
     * @param collection The Collection to add at the end of the array.
     */
    public void addAll(Collection<? extends T> collection) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.addAll(collection);
            } else {
                mObjects.addAll(collection);
            }
        }
        notifyDataSetChanged();
    }
    
    /**
     * Adds the specified items at the end of the array.
     *
     * @param items The items to add at the end of the array.
     */
    public void addAll(T ... items) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                Collections.addAll(mOriginalValues, items);
            } else {
                Collections.addAll(mObjects, items);
            }
        }
        notifyDataSetChanged();
    }
    
    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object The object to insert into the array.
     * @param index The index at which the object must be inserted.
     */
    public void insert(T object, int index) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(index, object);
            } else {
                mObjects.add(index, object);
            }
        }
        notifyDataSetChanged();
    }
    
    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     */
    public void remove(T object) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.remove(object);
            } else {
                mObjects.remove(object);
            }
        }
        notifyDataSetChanged();
    }
    
    /**
     * Removes the object at the specified position for the array.
     * 
     * @param position The position of the object to remove
     */
    public void remove(int position) {
    	synchronized (mLock) {
    		if (mOriginalValues != null) {
                mOriginalValues.remove(position);
            } else {
                mObjects.remove(position);
            }
        }
        notifyDataSetChanged();
    }
    
    /**
     * Remove all elements from the list.
     */
    public void clear() {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.clear();
            } else {
                mObjects.clear();
            }
        }
        notifyDataSetChanged();
    }
    
    /**
     * Moves the object at the specified position in the array to a new position.
     * 
     * @param from The original position of the object to be moved
     * @param to The new position to place the object
     */
    public void move(int from, int to) {
    	if (from != to) {
    		synchronized (mLock) {
                if (mOriginalValues != null) {
                    mOriginalValues.add(to, mOriginalValues.remove(from));
                } else {
                    mObjects.add(to, mObjects.remove(from));
                }
            }
            notifyDataSetChanged();
    	}
    }
    
    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained
     *        in this adapter.
     */
    public void sort(Comparator<? super T> comparator) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                Collections.sort(mOriginalValues, comparator);
            } else {
                Collections.sort(mObjects, comparator);
            }
        }
        notifyDataSetChanged();
    }
	
	@Override
	public int getCount() {
		return mObjects.size();
	}

	@Override
	public T getItem(int position) {
		return mObjects.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v;
		if (convertView == null) {
			v = newView(mContext, getItemViewType(position), parent);
		} else {
			v = convertView;
		}
		bindView(v, mContext, getItem(position));
		return v;
	}
	
	@Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = newDropDownView(mContext, getItemViewType(position), parent);
        } else {
            v = convertView;
        }
        bindView(v, mContext, getItem(position));
        return v;
    }
	
	/**
     * Makes a new view to hold the data.
     * @param context Interface to application's global information
     * @param viewType The view type to use to create a new view
     * @param parent The parent to which the new view is attached to
     * @return the newly created view.
     */
	public abstract View newView(Context context, int viewType, ViewGroup parent);
	
	/**
     * Makes a new drop down view to hold the data.
     * @param context Interface to application's global information
     * @param viewType The view type to use to create a new view
     * @param parent The parent to which the new view is attached to
     * @return the newly created view.
     */
	public View newDropDownView(Context context, int viewType, ViewGroup parent) {
		return newView(context, viewType, parent);
	}
	
	/**
     * Bind an existing view to the object found in the array at the current position
     * @param view Existing view, returned earlier by newView
     * @param context Interface to application's global information
     * @param item The object from which to get the data.
     */
	public abstract void bindView(View view, Context context, T item);

	@Override
	public Filter getFilter() {
		if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
	}
	
	/**
     * <p>An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.</p>
     */
    private class ArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<T>(mObjects);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<T> list;
                synchronized (mLock) {
                    list = new ArrayList<T>(mOriginalValues);
                }
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<T> values;
                synchronized (mLock) {
                    values = new ArrayList<T>(mOriginalValues);
                }

                final int count = values.size();
                final ArrayList<T> newValues = new ArrayList<T>();

                for (int i = 0; i < count; i++) {
                    final T value = values.get(i);
                    final String valueText = value.toString().toLowerCase();

                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(value);
                    } else {
                        final String[] words = valueText.split(" ");
                        final int wordCount = words.length;

                        // Start at index 0, in case valueText starts with space(s)
                        for (int k = 0; k < wordCount; k++) {
                            if (words[k].startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @SuppressWarnings("unchecked")
		@Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mObjects = (List<T>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
