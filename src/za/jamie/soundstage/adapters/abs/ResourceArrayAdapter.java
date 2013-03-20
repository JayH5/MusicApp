package za.jamie.soundstage.adapters.abs;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public abstract class ResourceArrayAdapter<T> extends BaseAdapter {

	/**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private List<T> mObjects;

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter.
     */
    private int mResource;

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter in a drop down widget.
     */
    private int mDropDownResource;

    private Context mContext;

    private LayoutInflater mInflater;

    /**
     * Constructor
     *
     * @param context The current context.
     * @param resource The resource ID for a layout file containing a layout to use when
     *                 instantiating views.
     * @param textViewResourceId The id of the TextView within the layout resource to be populated
     * @param objects The objects to represent in the ListView.
     */
    public ResourceArrayAdapter(Context context, int resource, T[] objects) {
    	
        init(context, resource, Arrays.asList(objects));
    }

    /**
     * Constructor
     *
     * @param context The current context.
     * @param resource The resource ID for a layout file containing a layout to use when
     *                 instantiating views.
     * @param textViewResourceId The id of the TextView within the layout resource to be populated
     * @param objects The objects to represent in the ListView.
     */
    public ResourceArrayAdapter(Context context, int resource, List<T> objects) {
        init(context, resource, objects);
    }
    
    private void init(Context context, int resource, List<T> objects) {
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResource = mDropDownResource = resource;
        mObjects = objects;
    }    
    
	
	@Override
	public int getCount() {
		if (mObjects != null) {
			return mObjects.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (mObjects != null) {
			return mObjects.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, mResource);
	}
	
	/**
     * {@inheritDoc}
     */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mDropDownResource);
    }
	
	private View createViewFromResource(int position, View convertView, ViewGroup parent,
            int resource) {
		
		View view;
		if (convertView == null) {
            view = mInflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }
		
		bindView(view, mContext, mObjects.get(position));
		
		return view;
	}
	
	public abstract void bindView(View view, Context context, T object);
	

    /**
     * Sets the layout resource of the item views.
     *
     * @param layout the layout resources used to create item views
     */
    public void setViewResource(int resource) {
        mResource = resource;
    }
    
    /**
     * Sets the layout resource of the drop down views.
     *
     * @param dropDownLayout the layout resources used to create drop down views
     */
    public void setDropDownViewResource(int dropDownResource) {
        mDropDownResource = dropDownResource;
    }
    
    public void setList(List<T> objects) {
    	if (objects != null) {
    		mObjects = objects;
    		notifyDataSetChanged();
    	} else {
    		mObjects = null;
    		notifyDataSetInvalidated();
    	}
    }
    
    public void setArray(T[] objects) {
    	if (objects != null) {
    		setList(Arrays.asList(objects));
    	} else {
    		setList(null);
    	}
    }

}
