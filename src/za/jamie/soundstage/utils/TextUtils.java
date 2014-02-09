package za.jamie.soundstage.utils;

import za.jamie.soundstage.R;
import android.annotation.SuppressLint;
import android.content.res.Resources;

public final class TextUtils {

    private TextUtils() {}
	
	public static String getYearText(int firstYear, int lastYear) {
		final StringBuilder sb = new StringBuilder();
		if (firstYear > 0) {
			sb.append(firstYear);
			if (lastYear > 0 && firstYear != lastYear) {
				sb.append(" - ");
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

    public static String getNumArtistsText(Resources res, int numArtists) {
        return res.getQuantityString(R.plurals.artists, numArtists, numArtists);
    }
	
	public static String getNumAlbumsText(Resources res, int numAlbums) {
		return res.getQuantityString(R.plurals.albums, numAlbums, numAlbums);
	}
	
	public static String getNumTracksText(Resources res, int numTracks) {
		return res.getQuantityString(R.plurals.tracks, numTracks, numTracks);
	}
	
	public static String getTrackNumText(int trackNum) {
		if (trackNum < 1) {
			return "--";
		} else {
			return String.format("%02d", trackNum % 100);
		}
	}
	
	@SuppressLint("DefaultLocale")
	public static String getTrackDurationText(long duration) {
		int seconds = (int) (duration / 1000) % 60 ;
		int minutes = (int) ((duration / (1000*60)) % 60);
		int hours   = (int) (duration / (1000*60*60));
		
		if (hours > 0) {
			return String.format("%d:%02d:%02d", 
					hours, minutes, seconds);
		} else {
			return String.format("%02d:%02d", 
					minutes, seconds);
		}
	}
	
	/**
	 * Grab a bit of functionality out of MediaStore.Audio.keyFor(). This
	 * is literally all it does to strip out the/an/a and special characters. 
	 * There doesn't appear to be any internationalization (as of 4.2.2r1).
	 * @param name
	 * @return
	 */
	public static String headerFor(String name) {
		name = name.trim().toUpperCase();
        if (name.startsWith("THE ")) {
        	name = name.substring(4);
        }
        if (name.startsWith("AN ")) {
        	name = name.substring(3);
        }
        if (name.startsWith("A ")) {
        	name = name.substring(2);
        }
        
        // There are probably more characters
        name = name.replaceAll("[\\[\\]\\(\\)\"'.,?!]", "").trim();
        
        if (name.isEmpty() || !Character.isLetterOrDigit(name.charAt(0))) {
        	return "!?$";
        } else if (Character.isDigit(name.charAt(0))) {
        	return "123";
        } else {
        	return name.substring(0, 1);
        }
	}

}
