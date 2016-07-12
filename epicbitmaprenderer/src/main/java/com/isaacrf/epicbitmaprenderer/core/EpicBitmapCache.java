/*
 * Copyright (C) 2016 Isaac R.F.
 * http://isaacrf.com/works/epicbitmaprenderer
 * https://github.com/isaacrf/EpicBitmapRenderer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.isaacrf.epicbitmaprenderer.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.util.LruCache;

import com.isaacrf.epicbitmaprenderer.utils.DiskLruCache;

import java.io.File;
import java.io.IOException;

/**
 * An epic Bitmap cache to store data on both memory and disk cache. This class auto manages caches concurrency, size and exposes methods for handling
 */
public class EpicBitmapCache {
    //region Cache objects
    private LruCache<String, Bitmap> mMemoryCache;
    private DiskLruCache mDiskLruCache;
    //endregion Cache objects

    //region Config. fields
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "images";
    //endregion Config. fields

    /**
     * Basic constructor, builds the memory cache automatically.
     */
    public EpicBitmapCache() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        //Initialize the memory cache
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
            }
        };
    }

    //TODO: Find a way to automatically get the context (if possible) and move this piece of code to constructor, to avoid requesting user to explicitly call methods and pass context as argument

    /**
     * Initializes memory cache. This method uses context to find app's own cache directory.
     *
     * @param context {@link Context} from where lib is being called. This is used to get application's cache dir for disk cache.
     */
    public void initDiskCache(Context context) {
        // Initialize disk cache on background thread
        File cacheDir = getDiskCacheDir(context, DISK_CACHE_SUBDIR);
        new InitDiskCacheTask().execute(cacheDir);
    }

    //region Cache handling methods

    /**
     * Adds or updates a {@link Bitmap} to Memory and Disk cache compressed in format and quality specified, identified by a key.
     *
     * @param key                   ID of the {@link Bitmap} to retrieve it later.
     * @param bitmap                {@link Bitmap} to be stored.
     * @param inDiskCompressFormat  Format to compress the image (JPEG, PNG, etc.) to store on disk cache.
     * @param inDiskCompressQuality Compress quality percentage of the image, from 0 to 100, to store on disk cache.
     */
    public void put(String key, Bitmap bitmap, Bitmap.CompressFormat inDiskCompressFormat, int inDiskCompressQuality) {
        // Add to memory cache
        mMemoryCache.put(key, bitmap);

        // Also add to disk cache
        synchronized (mDiskCacheLock) {
            mDiskLruCache.put(key, bitmap, inDiskCompressFormat, inDiskCompressQuality);
        }
    }

    /**
     * Adds or updates a {@link Bitmap} to Memory and Disk cache compressed in format and quality specified, identified by a key.
     *
     * @param key                   ID of the {@link Bitmap} to retrieve it later.
     * @param bitmap                {@link Bitmap} to be stored.
     * @param outMimeType           Image's mime type (usually in form 'image/format', e.g. 'image/png'), to automatically obtain the compress format to store on disk cache.
     * @param inDiskCompressQuality Compress quality percentage of the image, from 0 to 100, to store on disk cache.
     * @throws IllegalArgumentException In case Mime Type specified is not image.
     */
    public void put(String key, Bitmap bitmap, String outMimeType, int inDiskCompressQuality)
            throws IllegalArgumentException {
        Bitmap.CompressFormat inDiskCompressFormat;
        String[] parts = outMimeType.split("/");

        //Mime type should be "image"
        if (parts[0].equals("image")) {
            if (parts[1].contains("jpeg")) {
                inDiskCompressFormat = Bitmap.CompressFormat.JPEG;
            } else {
                inDiskCompressFormat = Bitmap.CompressFormat.PNG;
            }

            put(key, bitmap, inDiskCompressFormat, inDiskCompressQuality);
        } else {
            throw new IllegalArgumentException("Incorrect Mime Type. Expected image, found " + parts[0]);
        }
    }

    /**
     * Adds or updates a {@link Bitmap} to Memory and Disk cache identified by a key.
     * <p>
     * <b>NOTE:</b> This method uses JPEG format 100% quality by default when compressing image to store it on disk cache.
     * To manually specify format and quality of compression in disk cache, use {@link #put(String, Bitmap, Bitmap.CompressFormat, int)} instead.
     *
     * @param key    ID of the {@link Bitmap} to retrieve it later
     * @param bitmap {@link Bitmap} to be stored
     */
    public void put(String key, Bitmap bitmap) {
        put(key, bitmap, Bitmap.CompressFormat.JPEG, 100);
    }

    /**
     * Removes an entry from memory and disk cache.
     * @param key Identifier of value to be removed from cache.
     * @throws IOException In case entry could not be removed from disk cache due to an IO Error.
     */
    public void remove(String key) throws IOException {
        mMemoryCache.remove(key);

        synchronized (mDiskCacheLock) {
            mDiskLruCache.remove(key);
        }
    }

    /**
     * Gets Bitmap from Memory Cache by its key.
     *
     * @param key ID of the {@link Bitmap} to be retrieved.
     * @return {@link Bitmap} object if found, null otherwise.
     */
    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    /**
     * Gets Bitmap from Disk Cache by its key.
     *
     * @param key ID of the {@link Bitmap} to be retrieved.
     * @return {@link Bitmap} object if found, null otherwise.
     */
    public Bitmap getBitmapFromDiskCache(String key) {
        synchronized (mDiskCacheLock) {
            // Wait while disk cache is started from background thread
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException ignored) {
                }
            }
            if (mDiskLruCache != null) {
                return mDiskLruCache.getBitmap(key);
            }
        }
        return null;
    }

    /**
     * Tries to retrieve a Bitmap by its key from Memory Cache, and if not found, from Disk Cache.
     *
     * @param key ID of the {@link Bitmap} to be retrieved.
     * @return {@link Bitmap} object if found, null otherwise.
     */
    public Bitmap getBitmapFromCache(String key) {
        Bitmap bitmap = null;

        if (mMemoryCache != null) {
            bitmap = getBitmapFromMemCache(key);
        }
        if (bitmap == null && mDiskLruCache != null) {
            bitmap = getBitmapFromDiskCache(key);
        }

        return bitmap;
    }

    /**
     * Deletes memory and disk cache contents.
     *
     * @throws IOException In case disk cache could not be cleared due to an IO error.
     */
    public void clear() throws IOException {
        mMemoryCache.evictAll();

        synchronized (mDiskCacheLock) {
            mDiskLruCache.delete();
        }
    }
    //endregion Cache handling methods

    //region Helper methods

    /**
     * Creates a unique subdirectory of the designated app cache directory. Tries to use external
     * but if not mounted, falls back on internal storage.
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        File externalCacheDir = context.getExternalCacheDir();
        File cacheDir = context.getCacheDir();
        String cachePath;

        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        if ((Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) && externalCacheDir != null) {
            cachePath = externalCacheDir.getPath();
        } else {
            cachePath = cacheDir.getPath();
        }

        return new File(cachePath + File.separator + uniqueName);
    }
    //endregion Helper methods

    //region Helper classes

    /**
     * AsyncTask to initialize disk cache.
     */
    class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
        @Override
        protected Void doInBackground(File... params) {

            synchronized (mDiskCacheLock) {
                try {
                    File cacheDir = params[0];
                    mDiskLruCache = DiskLruCache.open(cacheDir, DISK_CACHE_SIZE);
                    mDiskCacheStarting = false; // Finished initialization
                    mDiskCacheLock.notifyAll(); // Wake any waiting threads
                } catch (IOException e) {
                }
            }

            return null;
        }
    }
    //endregion Helper classes
}
