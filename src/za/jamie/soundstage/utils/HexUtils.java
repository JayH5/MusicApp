package za.jamie.soundstage.utils;

import java.util.LinkedList;
import java.util.List;

public class HexUtils {

	public static final String DELIMITER = ";";
	
	public static List<Integer> hexStringToIntList(String hexString) {
    	final String[] hexes = hexString.split(DELIMITER);
    	final List<Integer> list = new LinkedList<Integer>();
    	for (String hex : hexes) {
    		list.add(Integer.parseInt(hex, 16));
    	}
    	return list;
    }
	
	public static List<Long> hexStringToLongList(String hexString) {
		final String[] hexes = hexString.split(DELIMITER);
    	final List<Long> list = new LinkedList<Long>();
    	for (String hex : hexes) {
    		list.add(Long.parseLong(hex, 16));
    	}
    	return list;
	}
	
	public static String intListToHexString(List<Integer> list) {
    	final StringBuilder builder = new StringBuilder();
    	for (int id : list) {
    		builder.append(Integer.toHexString(id));
    		builder.append(DELIMITER);
    	}
    	return builder.toString();
    }
	
	public static String longListToHexString(List<Long> list) {
    	final StringBuilder builder = new StringBuilder();
    	for (long id : list) {
    		builder.append(Long.toHexString(id));
    		builder.append(DELIMITER);
    	}
    	return builder.toString();
    }
}
