package za.jamie.soundstage.pablo;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.net.Uri;
import android.util.Log;
import android.util.Xml;

public class LastfmXmlParser {
	
	private static final String TAG = "LastfmXmlParser";
	
	// No namespace
    private static final String ns = null;

    /**
     * Parses a response from lastfm
     * @param in Stream containing xml response
     * @return A map containing the addresses of images of various sizes
     * @throws XmlPullParserException
     * @throws IOException
     */
	public static Map<String, Uri> parseImages(InputStream in)
			throws XmlPullParserException, IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readImages(parser);
		} finally {
			in.close();
		}
	}
	
	private static Map<String, Uri> readImages(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		Map<String, Uri> images = null;		
		parser.require(XmlPullParser.START_TAG, ns, "lfm");
		
		String status = parser.getAttributeValue(ns, "status");
		if (!status.equals("ok")) {
			Log.w(TAG, "Lastfm status not ok, status= " + status);
			return null;
		}
		
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("album")) {
				images = readAlbumImages(parser);
			} else if (name.equals("artist")) {
				images = readArtistImages(parser);
			} else {
				skip(parser);
			}
		}
		return images;
	}
	
	private static Map<String, Uri> readAlbumImages(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		Map<String, Uri> images = new HashMap<String, Uri>();
		parser.require(XmlPullParser.START_TAG, ns, "album");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			
			String name = parser.getName();
			if (name.equals("image")) {
				String size = parser.getAttributeValue(ns, "size");
				String text = null;
				if (parser.next() == XmlPullParser.TEXT) {
			        text = parser.getText();
			        parser.nextTag();
			    }
				Uri uri = Uri.parse(text);
				images.put(size, uri);
			} else {
				skip(parser);
			}
		}
		return images;
	}
	
	private static Map<String, Uri> readArtistImages(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		Map<String, Uri> images = new HashMap<String, Uri>();
		parser.require(XmlPullParser.START_TAG, ns, "artist");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			
			String name = parser.getName();
			if (name.equals("image")) {
				String size = parser.getAttributeValue(ns, "size");
				String text = null;
				if (parser.next() == XmlPullParser.TEXT) {
			        text = parser.getText();
			        parser.nextTag();
			    }
				Uri uri = Uri.parse(text);
				images.put(size, uri);
			} else {
				skip(parser);
			}
		}
		return images;
	}
	
	/**
	 * Skip over the contents of a tag
	 */
	private static void skip(XmlPullParser parser)
			throws XmlPullParserException, IOException {
	    if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
	        case XmlPullParser.END_TAG:
	            depth--;
	            break;
	        case XmlPullParser.START_TAG:
	            depth++;
	            break;
	        }
	    }
	}

}
