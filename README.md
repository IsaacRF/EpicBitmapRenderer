# EpicBitmapRenderer

[ ![Download](https://api.bintray.com/packages/isaacrf/maven/EpicBitmapRenderer/images/download.svg) ](https://bintray.com/isaacrf/maven/epicbitmaprenderer/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.isaacrf.epicbitmaprenderer/epicbitmaprenderer/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.isaacrf.epicbitmaprenderer/epicbitmaprenderer)

Decode and render Bitmaps the epic and easy way, creating faster Android apps without extra effort.

![EpicBitmapRenderer Icon](http://isaacrf.com/libs/epicbitmaprenderer/images/EpicBitmapRenderer-Icon.png)

## Epic Bitmap Renderer, the outOfMemoryError slayer
Have you ever had to face image decoding in an Android app? If you have, you have most definitely also faced the problem of having to repeat many tedious and time consuming tasks, each and every single time you have had to do so in a different app.

Following Google conventions on displaying bitmaps efficiently
(see [Google guide][gbmpdisplayguidelink] for more info), this Android Bitmap decoder library puts an end to these problems, automatizing most of these tedious tasks, and even others you didn't know you had to do!

Epic Bitmap Renderer offers Android developers the following features:

* **Exposes static asynchronous** (and synchronous too, just in case you need it) **methods** to decode Bitmap objects from different sources **outside the UI thread**, ensuring that your app runs smoothly.
* **Image auto and manual downsampling.** This library keeps memory usage of your app low by loading images in just the scale and size you need, only specifying Image holder's size (Or a manual downsample rate). If the device is still unable to load image due to low memory available, render methods will automatically recalculate image downsample for it to successfully fit in the device's memory, **avoiding that annoying OutOfMemoryError.**
* **Image auto caching.** Rendering methods automatically save rendered Bitmaps in memory and disk caches using dual cache EpicBitmapCache. If an image is previously rendered, next time it will be extracted from cache if available, and it will be used instead of re-rendering Bitmap from source again. This entire process is automatic (as render methods handle cache themselves) and saves a lot of memory consumption from heavy processes like rendering images from disk or the Internet.

## How to use Epic Bitmap Renderer in your app
You can access general [javadoc][jdoclink] to see project structure and documentation, or access directly to [EpicBitmapRenderer class javadoc][jdoclink2] to see all image decoding methods available, and how to call them.

The Android Studio Project contains a "samples" app module with a single Main Activity, that can be installed on an Android device or virtual machine to test library functions. It contains all the code required for importing, initializing and calling the library in your own app, as well as examples of library decoding method calls.

![Samples app screen capture](http://isaacrf.com/libs/epicbitmaprenderer/images/SamplesApp.png)

### 1.- Importing library
First of all, you need to import EpicBitmapRenderer library into your proyect. It is available on Bintray JCenter and Maven Central repositories. There are several ways to do this depending on your IDE and your project configuration.

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

You can also manually download the library .aar file (.jar + Android dependencies) and javadocs from [EpicBitmapRenderer JCenter repository][jcenterlink], add files to your project libs folder, and import the library using your IDE or Gradle, adding the following to your module's build.gradle script:

```groovy
repositories {
    flatDir {
        dirs 'libs'
    }
}

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    testCompile 'junit:junit:4.12'
    //Add .aar file to libs/ and use folder as repository
    compile(name:'epicbitmaprenderer-1.0', ext:'aar')
}
```

### 2.- Initializing the library
Once EpicBitmapRenderer is succesfully imported into your project, you will have access to the EpicBitmapRenderer class and its static methods to decode and render Bitmaps in your app.

EpicBitmapLibrary uses a dual memory and disk cache to improve image decoding and rendering processes. Memory cache is automatically initialized when the library is loaded. However, disk cache requires a context to know what your app's own cache folder is and create it, so it needs to be manually initialized. **(I'm still looking for ways to avoid this step and automatize the process, see "Known issues / todo features list" in the "Contributing" section for more info).**

So the first step is initializing disk cache, calling this method in your code (You may only call this method ONCE in the entire app life cycle). If disk cache is not initialized, **the library will continue to function properly and will keep storing decoded images in memory cache.**

```java
/* Pass context as parameter. You can use "this" inside 
an Activity, or "ActivityName.this" in other levels 
 */
EpicBitmapRenderer.initDiskCache(this);
```

To decode Bitmaps from files or URLs, your app may need to request special permissions.

### 3.- Decoding Bitmaps
EpicBitmapRenderer is a static class containing only static methods, so you don't need to instantiate it to use the library. Here is an example, extracted from samples app, of calling a method to decode a Bitmap from a resource of your app, and then showing it on an ImageView, or handling the decoding error, if one occurs.

```java
/*Sample 1: Decode Bitmap from Resource app icon, downsample if needed 
to fit in 200x200 ImageView,  (Async)
 */
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
                Toast.makeText(MainActivity.this, 
                        "Failed to load Bitmap from Resource: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
```

And that's it! EpicBitmapRenderer decodes the Bitmap asynchronously in a worker thread, stores it in memory and disk cache, and, if successful, returns decoded Bitmap at the end of the process on the onBitmapRendered(Bitmap) callback. Further calls to render methods pointing to the same resource, will obtain the decoded Bitmap from memory or disk cache if available, instead of rendering the resource again, thus saving memory usage.

Almost every decoding method has an alternate, overloaded synchronous method (same arguments without callbacks) in case you need them, but it's not recommended as they run on the UI thread and can freeze the app. Here is the same example as before, but calling the synchronous method:

```java
Bitmap decodedBitmap = EpicBitmapRenderer.decodeBitmapFromResource(getResources(), 
        R.mipmap.ic_launcher, 200, 200);
((ImageView) findViewById(R.id.imgSampleDecodeResource)).setImageBitmap(decodedBitmap);
```

## Contributing
You can help improve EpicBitmapRenderer in many ways, some of which are:

* Adding new image decoding methods, from different kinds of sources, to different image formats.
* Improving cache and other core elements' functionality.
* Reporting and/or fixing bugs, issues, etc.

Just fork the project, modify what you want, and make a pull request to master Branch. Please, follow library structure and coding style when contributing, and make sure that your code compiles.

If you add a new decoding method, please add an example of usage in samples app module as well, and update the documentation to match any changes you make.

### 1.- Project Structure
![Project Structure from Android Studio](http://isaacrf.com/libs/epicbitmaprenderer/images/ProjectStructure.png)

EpicBitmapRenderer is an Android Studio project divided in 2 main modules:

* epicbitmaprenderer
* samples


#### 1.1.- epicbitmaprenderer module
epicbitmaprenderer is an Android Library module containing all the library code. This module is divided into the following packages:

* **core:** This package stores all the core code of the library divided into 2 classes, EpicBitmapRenderer, containing all Bitmap decoding and rendering methods, and EpicBitmapCache, a dual cache class to handle automatic image caching on memory and disk.
* **asynctasks:** Different asynctasks used by core package, mainly by EpicBitmapRenderer to decode Bitmaps asynchronously.
* **listeners:** Interfaces for callback definitions. Used to expose a template to override callbacks and handle decoding results.
* **utils:** Helper classes used for different tasks, such as handling disk cache and performing IO reading/writing operations.
    

#### 1.2.- samples module
samples is an Android app module containing a single Main Activity with different Bitmap decoding examples. This app can be deployed on an Android device or virtual machine to test library functionalities.

When adding new decoding methods from new sources or to new formats, this activity must be updated with examples of method usage.

### 2.- Known issues / todo features list
- [ ] Find a way to initialize disk cache automatically on EpicBitmapCache (needs application context), without asking the user to call a method (initDiskCache) passing Context as parameter. Context is just used to retrieve app's cache dir
- [ ] Allow to clear cache or force rendering from source skipping cache check task
- [ ] Allow to enable / disable automatic image caching

[jcenterlink]: https://bintray.com/isaacrf/maven/EpicBitmapRenderer/1.0
[jdoclink]: http://epicbitmaprenderer.isaacrf.com/javadoc/
[jdoclink2]: http://epicbitmaprenderer.isaacrf.com/javadoc/com/isaacrf/epicbitmaprenderer/core/EpicBitmapRenderer.html
[gbmpdisplayguidelink]: https://developer.android.com/training/displaying-bitmaps/index.html?hl=es
