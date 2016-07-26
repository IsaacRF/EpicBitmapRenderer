# EpicBitmapRenderer
<p>
Decode and render Bitmaps the epic and easy way, creating faster Android apps without extra effort.
</p>
![](http://isaacrf.com/libs/EpicBitmapRenderer/images/EpicBitmapRenderer-Icon.png)

<h2>Epic Bitmap Renderer, the outOfMemoryError slayer</h2>
<p>
Have you ever had to face image decoding in an Android app? If you have, you have most definitely also faced the problem of having to repeat many tedious and time consuming tasks, each and every single time you have had to do so in a different app.
</p>
<p>
Following Google conventions on displaying bitmaps efficiently
(see <a href="https://developer.android.com/training/displaying-bitmaps/index.html?hl=es">Google guide</a> for more info), this Android Bitmap decoder library puts an end to these problems, automatizing most of these tedious tasks, and even others you didn't know you had to do!
</p>
<p>
Epic Bitmap Renderer offers Android developers the following features:
</p>
<ul>
    <li>
        <b>Exposes static asynchronous</b> (and synchronous too, just in case you need it) <b>methods</b> to decode Bitmap objects from different sources <b>outside the UI thread</b>, ensuring that your app runs smoothly.
    </li>
    <li>
        <b>Image auto and manual downsampling.</b> This library keeps memory usage of your app low by loading images in just the scale and size you need, only specifying Image holder's size (Or a manual downsample rate). If the device is still unable to load image due to low memory available, render methods will automatically recalculate image downsample for it to successfully fit in the device's memory, <b>avoiding that annoying OutOfMemoryError.</b>
    </li>
    <li>
        <b>Image auto caching.</b> Rendering methods automatically save rendered Bitmaps in memory and disk caches using dual cache EpicBitmapCache. If an image is previously rendered, next time it will be extracted from cache if available, and it will be used instead of re-rendering Bitmap from source again. This entire process is automatic (as render methods handle cache themselves) and saves a lot of memory consumption from heavy processes like rendering images from disk or the Internet.
    </li>
</ul>

<h2>How to use Epic Bitmap Renderer in your app</h2>

The Android Studio Project contains a "samples" app module with a single Main Activity, that can be installed on an Android device or virtual machine to test library functions. It contains all the code required for importing, initializing and calling the library in your own app, as well as examples of library decoding method calls.

![](http://isaacrf.com/libs/EpicBitmapRenderer/images/SamplesApp.png)

<h3>1.- Importing library</h3>
<p>First of all, you need to import EpicBitmapRenderer library into your proyect. It is available on Bintray JCenter and Maven Central repositories. There are several ways to do this depending on your IDE and your project configuration.</p>

Gradle:
```groovy
compile 'com.isaacrf.epicbitmaprenderer:epicbitmaprenderer:1.0'
```

Maven:
```xml
<dependency>
  <groupId>com.isaacrf.epicbitmaprenderer</groupId>
  <artifactId>epicbitmaprenderer</artifactId>
  <version>1.0</version>
  <type>pom</type>
</dependency>
```

Ivy:
```xml
<dependency org='com.isaacrf.epicbitmaprenderer' name='epicbitmaprenderer' rev='1.0'>
  <artifact name='$AID' ext='pom'></artifact>
</dependency>
```

You can also manually download the library .aar file (.jar + Android dependencies) and javadocs from [EpicBitmapRenderer JCenter repository][1], add files to your project libs folder, and import the library using your IDE or Gradle, adding the following to your module's build.gradle script:

```groovy
repositories {
    flatDir {
        dirs 'libs'
    }
}

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    testCompile 'junit:junit:4.12'
    compile(name:'epicbitmaprenderer-1.0', ext:'aar')       //Add .aar file to libs/ and use folder as repository
}
```

<h3>2.- Initializing the library</h3>
<p>Once EpicBitmapRenderer is succesfully imported into your project, you will have access to the EpicBitmapRenderer class and its static methods to decode and render Bitmaps in your app.</p>

<p>EpicBitmapLibrary uses a dual memory and disk cache to improve image decoding and rendering processes. Memory cache is automatically initialized when the library is loaded. However, disk cache requires a context to know what your app's own cache folder is and create it, so it needs to be manually initialized. <b>(I'm still looking for ways to avoid this step and automatize the process, see "Known issues / todo features list" in the "Contributing" section for more info).</b></p>

<p>So the first step is initializing disk cache, calling this method in your code (You may only call this method ONCE in the entire app life cycle). If disk cache is not initialized, <b>the library will continue to function properly and will keep storing decoded images in memory cache.</b></p>

```java
//Pass context as parameter. You can use "this" inside an Activity, or "ActivityName.this" in other levels 
EpicBitmapRenderer.initDiskCache(this);
```

<p>To decode Bitmaps from files or URLs, your app may need to request special permissions</p>

<h3>3.- Decoding Bitmaps</h3>
<p>EpicBitmapRenderer is a static class containing only static methods, so you don't need to instantiate it to use the library. Here is an example, extracted from samples app, of calling a method to decode a Bitmap from a resource of your app, and then showing it on an ImageView, or handling the decoding error, if one occurs.</p>

```java
//Sample 1: Decode Bitmap from Resource app icon, downsample if needed to fit in 200x200 ImageView,  (Async)
EpicBitmapRenderer.decodeBitmapFromResource(getResources(), R.mipmap.ic_launcher, 200, 200,
        new OnBitmapRendered() {
            @Override
            public void onBitmapRendered(Bitmap bitmap) {
                //Display rendered Bitmap when successfully decoded
                ((ImageView) findViewById(R.id.imgSampleDecodeResource)).setImageBitmap(bitmap);
            }
        },
        new OnBitmapRenderFailed() {
            @Override
            public void onBitmapRenderFailed(Exception e) {
                //Take actions if Bitmap fails to render
                Toast.makeText(MainActivity.this, "Failed to load Bitmap from Resource: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
```

And that's it! EpicBitmapRenderer decodes the Bitmap asynchronously in a worker thread, stores it in memory and disk cache, and, if successful, returns decoded Bitmap at the end of the process on the onBitmapRendered(Bitmap) callback. Further calls to render methods pointing to the same resource, will obtain the decoded Bitmap from memory or disk cache if available, instead of rendering the resource again, thus saving memory usage.

Almost every decoding method has an alternate, overloaded synchronous method (same arguments without callbacks) in case you need them, but it's not recommended as they run on the UI thread and can freeze the app. Here is the same example as before, but calling the synchronous method:

```java
Bitmap decodedBitmap = EpicBitmapRenderer.decodeBitmapFromResource(getResources(), R.mipmap.ic_launcher, 200, 200);
((ImageView) findViewById(R.id.imgSampleDecodeResource)).setImageBitmap(decodedBitmap);
```

<h2>Contributing</h2>
<p>You can help improve EpicBitmapRenderer in many ways, some of which are:</p>
<ul>
    <li>Adding new image decoding methods, from different kinds of sources, to different image formats.</li>
    <li>Improving cache and other core elements' functionality.</li>
    <li>Reporting and/or fixing bugs, issues, etc.</li>
</ul>

<p>Just fork the project, modify what you want, and make a pull request to master Branch. Please, follow library structure and coding style when contributing, and make sure that your code compiles.</p>

<p>If you add a new decoding method, please add an example of usage in samples app module as well, and update the documentation to match any changes you make.</p>

<h3>1.- Project Structure</h3>
![](http://isaacrf.com/libs/EpicBitmapRenderer/images/ProjectStructure.png)

<p>EpicBitmapRenderer is an Android Studio project divided in 2 main modules:</p>
<ul>
    <li>epicbitmaprenderer</li>
    <li>samples</li>
</ul>

<h4>1.1.- epicbitmaprenderer module</h4>
<p>epicbitmaprenderer is an Android Library module containing all the library code. This module is divided into the following packages:</p>
<ul>
    <li><b>core:</b> This package stores all the core code of the library divided into 2 classes, EpicBitmapRenderer, containing all Bitmap decoding and rendering methods, and EpicBitmapCache, a dual cache class to handle automatic image caching on memory and disk.</li>
    <li><b>asynctasks:</b> Different asynctasks used by core package, mainly by EpicBitmapRenderer to decode Bitmaps asynchronously.</li>
    <li><b>listeners:</b> Interfaces for callback definitions. Used to expose a template to override callbacks and handle decoding results.</li>
    <li><b>utils:</b> Helper classes used for different tasks, such as handling disk cache and performing IO reading/writing operations.</li>
</ul>

<h4>1.2.- samples module</h4>
<p>samples is an Android app module containing a single Main Activity with different Bitmap decoding examples. This app can be deployed on an Android device or virtual machine to test library functionalities.</p>

<p>When adding new decoding methods from new sources or to new formats, this activity must be updated with examples of method usage.</p>

<h3>2.- Known issues / todo features list</h3>
<ul>
    <li>[ ! ] Find a way to initialize disk cache automatically on EpicBitmapCache (needs application context), without asking the user to call a method (initDiskCache) passing Context as parameter. Context is just used to retrieve app's cache dir</li>
    <li>[ ! ] Allow to clear cache or force rendering from source skipping cache check task</li>
    <li>[~] Allow to enable / disable automatic image caching</li>
</ul>

[1]: https://bintray.com/isaacrf/maven/EpicBitmapRenderer/1.0
