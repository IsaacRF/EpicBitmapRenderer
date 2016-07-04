# EpicBitmapRenderer
Decode and render Bitmaps the epic and easy way, creating faster apps without extra effort.

This library follows the Google conventions for displaying bitmaps efficiently (see <a href="https://developer.android.com/training/displaying-bitmaps/index.html?hl=es">Google guide</a> for more info), exposing static
methods to decode Bitmap objects from different sources using the minimum memory space possible, auto caching rendered
images for faster access and auto-avoiding out of memory errors recalculating the required image downsample if device is not able
to hold the original image due to low memory space available.
