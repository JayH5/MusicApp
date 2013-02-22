package za.jamie.soundstage.utils;

import za.jamie.soundstage.R;
import android.content.res.Resources;

public class TextUtils {
	
	public static String getYearText(Resources res, int firstYear, int lastYear) {
		final StringBuilder sb = new StringBuilder();
		if (firstYear > 0) {
			sb.append(firstYear);
			if (lastYear > 0 && firstYear != lastYear) {
				sb.append(res.getString(R.string.year_delimiter));
				sb.append(lastYear);
			}
		} else if (lastYear > 0) {
			sb.append(lastYear);
		}
		return sb.toString();
	}
	
	public static String getStatsDurationText(Resources res, long duration) {
		int seconds = (int) (duration / 1000) % 60 ;
		int minutes = (int) ((duration / (1000*60)) % 60);
		int hours = (int) (duration / (1000*60*60) % 24);
		int days = (int) (duration / (1000*60*60*24));
		
		String[] durationUnits = res.getStringArray(R.array.duration_units);
		
		String durationText;
		if (days > 0) {
			durationText = String.format("%d %S %02d %S %02d %S %02d %S", 
					days, durationUnits[3], 
					hours, durationUnits[2], 
					minutes, durationUnits[1], 
					seconds, durationUnits[0]);
		} else if (hours > 0) {
			durationText = String.format("%2d %S %02d %S %02d %S", 
					hours, durationUnits[2], 
					minutes, durationUnits[1], 
					seconds, durationUnits[0]);
		} else if (minutes > 0) {
			durationText = String.format("%2d %S %02d %S", 
					minutes, durationUnits[1], 
					seconds, durationUnits[0]);
		} else {
			durationText = String.format("%2d %S", 
					seconds, durationUnits[0]);
		}
		
		return durationText;
	}
	
	public static String getNumAlbumsText(Resources res, int numAlbums) {
		return res.getQuantityString(R.plurals.albums, numAlbums, numAlbums)
				.toUpperCase();
	}
	
	public static String getNumTracksText(Resources res, int numTracks) {
		return res.getQuantityString(R.plurals.tracks, numTracks, numTracks)
				.toUpperCase();
	}
	
	public static String getTrackNumText(Resources res, int trackNum) {
		if (trackNum < 1) {
			return res.getString(R.string.track_number_missing);
		} else {
			return String.format("%02d", trackNum % 100);
		}
	}
	
	public static String getTrackDurationText(Resources res, long duration) {
		int seconds = (int) (duration / 1000) % 60 ;
		int minutes = (int) ((duration / (1000*60)) % 60);
		int hours   = (int) (duration / (1000*60*60));
		
		String delimiter = res.getString(R.string.duration_delimiter);
		
		if (hours > 0) {
			return String.format("%d%s%02d%s%02d", 
					hours, delimiter, minutes, delimiter, seconds);
		} else {
			return String.format("%02d%s%02d", 
					minutes, delimiter, seconds);
		}
	}
}
