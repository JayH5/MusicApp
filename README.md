Soundstage
==========

The player that brings your music centre stage.
![Play screen](/screenshots/2013-11-01 13.50.26.png)


Dependencies
------------
* MenuDrawer 2.0.3 (https://github.com/SimonVT/android-menudrawer)
* Picasso ~2.1.2 (requires my fork with disk caching https://github.com/JayH5/picasso/tree/disk-cache)
* ~~drag-sort-listview 0.6.1 (https://github.com/bauerca/drag-sort-listview)~~ NO LONGER MAINTAINED. See my fork of the project: https://github.com/JayH5/drag-sort-listview

Requirements
------------
* Min API: 16
* Target API: 19

"Inspirations"
--------------
* Apollo Music player by Andrew Neal (https://github.com/adneal/Apollo-CM). A lot of Apollo, in turn is based on the original Android music player from Gingerbread, particularly the music service itself. Seeing how a music player is made was extremely useful. While I've rewritten just about 100% of the code I used from Apollo, it was really great to have something to base my work off.
* The source for the Android Gingerbread music player is here: https://github.com/android/platform_packages_apps_music
* The interface styling takes ques from Google Now as well as DeskClock in Jellybean 4.2

Assets
------
* The app does not have a proper logo yet. The one at the moment is pretty hideous.
* Loading images missing for albums/artists.
* A few other icons and such still need to be made/found. The current assets are mostly from the standard Android package (http://developer.android.com/downloads/design/Android_Design_Icons_20120814.zip).

Lastfm
------
* Lastfm is the source of album and artist images.

Current issues
--------------
* Playlist creation unimplemented
* Search bar does nothing (unimplemented)

Minor issues
------------
* ANDROID ISSUE: The MediaStore doesn't attribute albums to the correct artists in the case of compilations. Doesn't read the Album Artist metadata tag from the media file, instead adds the first artist of the album. FIXME: ??? Don't know how.
* In the album grid view, the adapter constantly reloads the image for the first album.. not sure why.. may be limiting performance.

Next features
-------------
* Long press song/album/artist/etc for contextual options
* Settings of some kind
* Widgets
* Idle notification
* Playlist editor
* Save queue as playlist
* ~~Play files from file explorer~~
* ~~Landscape layout~~
* ~~Multithreaded image loading~~

Would-be-nice-to-have-one-day features
--------------------------------------
* Tablet interface
* Music metadata editor
* Use lastfm API to magically get all the music metadata in tip-top shape
* Share e.g. tweet: "Losing My Edge by LCD Soundsystem off DFA Compilation #1 -- Soundstage for Android"
