package za.jamie.soundstage.fragments.dialogs;

import java.util.ArrayList;

import za.jamie.soundstage.PlaylistWorker;
import za.jamie.soundstage.models.Track;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;

public class CreatePlaylistDialog extends DialogFragment {

	public static final String EXTRA_TRACK_LIST = "extra_track_list";
	
	private EditText mEditText;
	
	public static CreatePlaylistDialog newInstance(ArrayList<Track> tracks) {
		final Bundle args = new Bundle();
		args.putParcelableArrayList(EXTRA_TRACK_LIST, tracks);
		
		CreatePlaylistDialog frag = new CreatePlaylistDialog();
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mEditText = new EditText(getActivity());
		mEditText.setSingleLine(true);
		
		final PlaylistWorker worker = new PlaylistWorker(getActivity());
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Enter playlist name:")
			.setView(mEditText)
			.setPositiveButton("Save", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					worker.createPlaylist(mEditText.getEditableText().toString(), 
							getArguments().getLongArray(EXTRA_TRACK_LIST));
					
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					
				}
			});
		
		return builder.create();
	}
	
	/*private class PlaylistSaveTask extends AsyncTask<Bundle, Void, Integer> {

		private final Context mContext;
		private final String mName;		
		
		public PlaylistSaveTask(Context context, String name) {
			mContext = context;
			mName = name;
		}
		
		@Override
		protected Integer doInBackground(Bundle... params) {
			final ContentResolver resolver = mContext.getContentResolver();
			
			// First insert the record of the playlist
			ContentValues values = new ContentValues();
			values.put(MediaStore.Audio.Playlists.NAME, mName);
			Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, 
					values);
			
			long playlistId = Long.parseLong(uri.getLastPathSegment());
			
			List<Track> trackList = params[0].getParcelableArrayList(EXTRA_TRACK_LIST);
			final int listLen = trackList.size();
			ContentValues[] valuesArray = null;
			if (trackList.size() > 1000) {
				for (int i = 0; i < trackList.size(); i += 1000) {
					int valuesArrayLen = listLen - i > 1000 ? 1000 : listLen - i;
					if (valuesArray == null || valuesArray.length != valuesArrayLen) {
						valuesArray = new ContentValues[valuesArrayLen];
					}
					for (int j = i; j < i + 1000; j++) {
						long id = trackList.get(j).writeToContentValues(valuesArray[j - i]);
						trackList
					}
				}
			}
			
			return null;
		}
		
	}*/
}
