package za.jamie.soundstage.adapters.interfaces;

import android.widget.Adapter;

public interface SearchableAdapter extends Adapter {
	/**
	 * Returns the position of the item specified by an id.
	 * @param id The id of the item to retrieve the position of.
	 * @return The position of the item with the specified id. -1 if that item is
	 *   not in the adapter's dataset.
	 */
	public int getItemPosition(long itemId);
}
