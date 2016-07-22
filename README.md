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

The Android Studio Project contains a "samples" app module with a single Main Activity, that can be installed on an Android device or virtual machine to test library functions. It contains all the code required for importing, initializing and using the library working on your own app, as well as examples of library decoding methods calls.

<h3>1.- Importing library</h3>
<p>First of all, you need to import EpicBitmapRenderer library into your proyect, available on Bintray JCenter and Maven Central repositories. There are several ways to do this depending on your IDE and your project configuration</p>

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

You can also manually download library .aar file (.jar + Android dependencies) and javadocs from [EpicBitmapRenderer JCenter repository][1], add files to your project libs folder, and import the library using your IDE or Gradle, adding the following to your module's build.gradle script:
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

<h3>2.- Initializing library</h3>
<p>Once EpicBitmapRenderer is succesfully imported into your project, you will have access to EpicBitmapRenderer class and its statics methods to decode and render Bitmaps on your app</p>

<p>EpicBitmapLibrary uses a dual memory and disk cache to improve image decoding and rendering process, memory cache is automatically initialized when library is loaded, but disk cache requires context to know your app's own cache folder and create it, so it needs to be manually initialized. <b>(I'm looking for ways to avoid this step and automatize the process, see the following "Contributing" section for more info)</b></p>

<p>So the first step is to initialize disk cache, calling this method on your code (You have to call this method just once in the entire app life cycle). If disk cache is not initialized, <b>the library will continue to function properly and storing decoded images on memory cache.</b></p>

```java
//Pass context as parameter. You can use "this" inside an Activity, or "ActivityName.this" in other levels 
EpicBitmapRenderer.initDiskCache(this);
```

<h3>3.- Calling library</h3>
<p>EpicBitmapRenderer is a static class containing static methods, so you don't need to instantiate the library to use it. Here is one example, extracted from samples app, of calling a method to decode a Bitmap from a resource of your app and show it on an ImageView, or handle the decoding error if one occurs.</p>

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

Almost every decoding method have an alternative synchronous method (same arguments without callbacks) in case you need them, but it's not recommended as they run on UI thread and can freeze the app. Here is the same example from before, calling the synchronous method:

```java
((ImageView) findViewById(R.id.imgSampleDecodeResource)).setImageBitmap(EpicBitmapRenderer.decodeBitmapFromResource(getResources(), R.mipmap.ic_launcher, 200, 200);
```

<h2>Contributing</h2>
<p>In process...</p>

[1]: https://bintray.com/isaacrf/maven/EpicBitmapRenderer/1.0
