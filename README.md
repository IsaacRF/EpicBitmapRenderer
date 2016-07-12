# EpicBitmapRenderer
<p>
Decode and render Bitmaps the epic and easy way, creating faster Android apps without extra effort.
</p>

<h2>Epic Bitmap Renderer, the outOfMemoryError slayer</h2>
<p>
This Android Bitmap decoder library follows the Google conventions for displaying bitmaps efficiently
(see <a href="https://developer.android.com/training/displaying-bitmaps/index.html?hl=es">Google guide</a> for more info), offering these features:
</p>
<ul>
    <li>
        <b>Exposes static asynchronous</b> (and synchronous too, just in case you need it) <b>methods</b> to decode Bitmap objects from different sources <b>out of UI thread</b>, ensuring that your app runs smoothly.
    </li>
    <li>
        <b>Image auto and manual downsampling.</b> This library keeps memory usage of your app low by loading images in just the scale and size you need, only specifying Image holder's size (Or a manual downsample rate). If device is still unable to load image due to low memory available, render methods will automatically recalculate image downsample for it to successfully fit on device's memory, <b>avoiding that annoying OutOfMemoryError</b>
    </li>
    <li>
        <b>Image auto caching.</b> Rendering methods automatically save rendered Bitmaps in memory and disk caches using dual cache EpicBitmapCache. If an image is previously rendered, next time it will be extracted from cache if available, and it will be used instead of re-rendering Bitmap from source again. This entire process is automatic, as render methods handle cache themselves, and saves a lot of memory consumption from heavy processes like rendering images from disk or internet.
    </li>
</ul>

<h2>How to use Epic Bitmap Renderer in your app</h2>
<p>In process...</p>

<h2>How can developers improve Epic Bitmap Renderer</h2>
<p>In process...</p>
