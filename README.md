Soundstage
==========

The player that brings your music centre stage.  
![Play screen](/screenshots/2013-05-23 18.23.43.png)


Dependencies
------------
* DiskLruCache 2.0.1 (https://github.com/JakeWharton/DiskLruCache)
* MenuDrawer 2.0.3 (https://github.com/SimonVT/android-menudrawer)
* ViewPagerIndicator 2.4.1 (https://github.com/JakeWharton/Android-ViewPagerIndicator)
* ~~drag-sort-listview 0.6.1 (https://github.com/bauerca/drag-sort-listview)~~ NO LONGER MAINTAINED. See my fork of the project: https://github.com/JayH5/drag-sort-listview
* Android Support Library v4 R13

Requirements
------------
* Min API: 16
* Target API: 17
* With a bit of work the app could be made to work back to API 14 (ICS) but taking it back any farther would be a huge amount of work.

"Inspirations"
--------------
* Apollo Music player by Andrew Neal (https://github.com/adneal/Apollo-CM). A lot of Apollo, in turn is based on the original Android music player from Gingerbread, particularly the music service itself. Seeing how a music player is made was extremely useful. While I've rewritten just about 100% of the code I used from Apollo, it was really great to have something to base my work off.
* The source for the Android Gingerbread music player is here: https://github.com/android/platform_packages_apps_music
* The image caching/loading is based on the Android dev guide code called "Bitmapfun" (http://developer.android.com/training/displaying-bitmaps/load-bitmap.html)
* The interface styling takes ques from Google Now as well as DeskClock in Jellybean 4.2

Assets
------
* The app does not have a proper logo yet. The one at the moment is pretty hideous.
* Loading images missing for albums/artists.
* A few other icons and such still need to be made/found. The current assets are mostly from the standard Android package (http://developer.android.com/downloads/design/Android_Design_Icons_20120814.zip).

Lastfm
------
* Lastfm is used to fetch album and artist images. The files in src/com/jamie/play/lastfm/ are based on the lastfm-java code (http://code.google.com/p/lastfm-java/)

Current issues
--------------
* Image caching framework still has a few issues. Service is only able to access the snapshot of the cache it opens when it is created. This means that when new images are added, the service doesn't see them until it is next started...which could take a while. There are some issues with the ImageDialog (shown when an album/artist image is clicked). The thumbnail cache needs to have its quality settings fine tuned.
* Playlist creation unimplemented
* Search bar does nothing (unimplemented)

Minor issues
------------
* ANDROID ISSUE: The MediaStore doesn't attribute albums to the correct artists in the case of compilations. Doesn't read the Album Artist metadata tag from the media file, instead adds the first artist of the album. FIXME: ??? Don't know how.
* In the album grid view, the adapter constantly reloads the image for the first album.. not sure why.. may be limiting performance.

Next features
-------------
* Long press song/album/artist/etc for contextual options
* Multi-threaded image processing/fetching from cache/downloading. See new Android training sample: http://developer.android.com/shareables/training/ThreadSample.zip
* Settings of some kind
* Landscape layout
* Widgets
* ~~Play files from file explorer~~

Would-be-nice-to-have-one-day features
--------------------------------------
* Tablet interface
* Music metadata editor
* Use lastfm API to magically get all the music metadata in tip-top shape
* Share e.g. tweet: "Losing My Edge by LCD Soundsystem off DFA Compilation #1 -- Soundstage for Android"
