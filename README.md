MusicApp
========

A music player that brings your music to centre stage.

Name
----
Previous working name: "Play". Current working name: "Soundstage".

Dependencies
------------
* DiskLruCache 1.3.1 (https://github.com/JakeWharton/DiskLruCache)
* MenuDrawer 2.0.0 (https://github.com/SimonVT/android-menudrawer)
* ViewPagerIndicator 2.4.1 (https://github.com/JakeWharton/Android-ViewPagerIndicator)
* Android Support Library v4 R11

"Inspirations"
--------------
* A fair portion of the code for this app is based on Apollo Music player by Andrew Neil (https://github.com/adneal/Apollo-CM). A lot of Apollo, in turn is based on the original Android music player from Gingerbread, particularly the music service itself. But a very significant portion of this app is original work.
* The image caching/loading is based on the Android dev guide code called "Bitmapfun" (http://developer.android.com/training/displaying-bitmaps/load-bitmap.html)
* The interface styling takes ques from Google Now for Jelly Bean

Assets
------
* The app does not have a proper logo yet. The one at the moment is pretty hideous.
* Loading images missing for albums/artists.
* A few other icons and such still need to be made/found. The current assets are mostly from the standard Android package (http://developer.android.com/downloads/design/Android_Design_Icons_20120814.zip).
* I know a guy who's an industrial designer and also does some graphic design... keep meaning to send him an email.

Lastfm
------
* Lastfm is used to fetch album and artist images. The files in src/com/jamie/play/lastfm/ are based on the lastfm-java code (http://code.google.com/p/lastfm-java/)
* The API key used was "borrowed" from Apollo. Please for god's sake remember to get own API key before releasing....

Current issues
--------------
* App fails to restore state in all activities other than the Library activity. This causes a fc when resuming the app from the album or artist browser. FIXME: Need to ensure these activities can recover themselves.
* Rotation crashes the app. FIXME: Need to lock app to portrait only for now. Must design new layouts for horizontal views.
* Image fetching mechanism not robust enough. Needs optimizing. Doesn't crash but there are a number of issues that will be pointed out in the code. FIXME: Work on making fetches from Content Uris really robust but hopefully without reopening an InputStream from the Content Resolver. Find a way to make the ImageDownloadService let image views know when it's fetched their images. Also improve the efficiency of the service by preventing large queues of incoming intents. ImageFetcher needs to be aware of what the service has already received requests for.
* For artists who are only featured on an album by another artist (happens a lot with compilations), browsing the artist will bring up an empty list of albums but say that the artist has some albums.
* ANDROID ISSUE: The MediaStore doesn't attribute albums to the correct artists in the case of compilations. Doesn't read the Album Artist metadata tag from the media file, instead adds the first artist of the album. FIXME: ??? Don't know how.
* Track seekbar, time indicators are UNIMPLEMENTED
* Shuffle/repeat modes are UNIMPLEMENTED, although some of the code is in the MusicService. Repeat mode really a matter of adding a button
* Notification and lockscreen controls show themselves inconsistently.
* When the play queue is brought up it is not set to the current position.
* The player drawer should show itself when the user selects a song to play.
* Album artwork and artist images are really low res... not sure why. But this is a pretty minor issue.

Next features
-------------
* Playlist creation
* Long press song/album/artist/etc for contextual options
* Play queue interface upgrade: Drag tracks to reorder. Press button to remove track from queue
* Settings of some kind

Would-be-nice-to-have-one-day features
--------------------------------------
* Tablet interface
* Music metadata editor
* Use lastfm API to magically get all the music metadata in tip-top shape
* Share e.g. tweet: "Losing My Edge by LCD Soundsystem off DFA Compilation #1 -- Soundstage for Android"
