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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.isaacrf.epicbitmaprenderer.asynctasks.AsyncDecodeFileForced;
import com.isaacrf.epicbitmaprenderer.asynctasks.AsyncDecodeFileMeasured;
import com.isaacrf.epicbitmaprenderer.asynctasks.AsyncDecodeResForced;
import com.isaacrf.epicbitmaprenderer.asynctasks.AsyncDecodeResMeasured;
import com.isaacrf.epicbitmaprenderer.asynctasks.AsyncDecodeUrlForced;
import com.isaacrf.epicbitmaprenderer.asynctasks.AsyncDecodeUrlMeasured;
import com.isaacrf.epicbitmaprenderer.listeners.OnBitmapRenderFailed;
import com.isaacrf.epicbitmaprenderer.listeners.OnBitmapRendered;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * <p>
 * Decode and render Bitmaps the epic and easy way, creating faster Android apps without extra effort.
 * </p>
 * <p>
 * This Android Bitmap decoder library follows the Google conventions for displaying bitmaps efficiently
 * (see <a href="https://developer.android.com/training/displaying-bitmaps/index.html?hl=es">Google guide</a> for more info), offering these features:
 * </p>
 * <ul>
 *     <li>
 *         <b>Exposes static asynchronous</b> (and synchronous too, just in case you need it) <b>methods</b> to decode {@link Bitmap} objects from different sources <b>out of UI thread</b>,
 *         ensuring that your app runs smoothly.
 *     </li>
 *     <li>
 *         <b>Image auto and manual downsampling.</b> This library keeps memory usage of your app low by loading images in just the scale and size you need, only
 *         specifying Image holder's size (Or a manual downsample rate). If device is still unable to load image due to low memory available, render methods
 *         will automatically recalculate image downsample for it to successfully fit on device's memory, <b>avoiding that annoying {@link OutOfMemoryError}</b>
 *     </li>
 *     <li>
 *         <b>Image auto caching.</b> Rendering methods automatically save rendered Bitmaps in memory and disk caches using dual cache {@link EpicBitmapCache}. If an image
 *         is previously rendered, next time it will be extracted from cache if available, and it will be used instead of re-rendering Bitmap from source again. This entire
 *         process is automatic, as render methods handle cache themselves, and saves a lot of memory consumption from heavy processes like rendering images from disk or internet.
 *     </li>
 * </ul>
 */
public final class EpicBitmapRenderer {
    //region Fields
    //TODO: Allow to enable / disable cache usage
    private static EpicBitmapCache epicBitmapCache;
    //endregion Fields

    //region Constructors and initialization

    /**
     * Library initializations. This block is called when library is loaded.
     */
    static {
        epicBitmapCache = new EpicBitmapCache();
    }

    /**
     * This class requires no instances, all methods are static and can be called following
     * the pattern EpicBitmapRenderer.methodName();
     */
    private EpicBitmapRenderer() {
    }

    //endregion Constructors and initialization

    //region Getters / Setters

    /**
     * Gets the instance of the current {@link EpicBitmapCache} object being used as cache.
     *
     * @return Cache in use.
     */
    public static EpicBitmapCache getCache() {
        return epicBitmapCache;
    }

    /**
     * Sets {@link EpicBitmapCache} object to use as cache.
     *
     * @param epicBitmapCache Cache to use by renderer
     */
    public static void setCache(EpicBitmapCache epicBitmapCache) {
        EpicBitmapRenderer.epicBitmapCache = epicBitmapCache;
    }

    //endregion Getters / Setters

    //region Rendering Synchronous Methods

    /**
     * <p>
     * Decodes a sampled {@link Bitmap} object from a given app resource, using the specified measures to calculate image downsample if needed.
     * Downsample rate is auto-increased if bitmap rendering causes an {@link OutOfMemoryError}.
     * </p>
     * <p>
     * <b>Important Note:</b> This method is synchronous and can cause UI Thread to freeze,
     * use {@link #decodeBitmapFromResource(Resources, int, int, int, OnBitmapRendered, OnBitmapRenderFailed)} instead for an asynchronous solution.
     * </p>
     *
     * @param res       Resources package. You can get default resources package using {@link Activity#getResources()} inside an activity or {@link Context#getResources()} outside if a {@link Context} is available.
     * @param resId     App resource id. Could be either the pure integer value, or the Android resource name (R.drawable.img_name).
     * @param reqWidth  Required width of the view where the Bitmap should fit. This parameter doesn't affect image aspect ratio, it's only used to calculate the inSampleSize of the image in case a downsample is required.
     * @param reqHeight Required height of the view where the Bitmap should fit. This parameter doesn't affect image aspect ratio, it's only used to calculate the inSampleSize of the image in case a downsample is required.
     * @return Decoded {@link Bitmap} object, ready to use on any View or code.
     */
    public static Bitmap decodeBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        Bitmap decodedBitmap = null;
        Boolean outOfMemoryError = true;

        // Search bitmap on cache first if available
        if (epicBitmapCache != null) {
            decodedBitmap = epicBitmapCache.getBitmapFromCache(String.valueOf(resId));
        }

        //If bitmap not found on cache, render it
        if (decodedBitmap == null) {
            // First decode with inJustDecodeBounds=true (No memory allocation) to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(res, resId, options);

            // Calculate inSampleSize
            options.inSampleSize = EpicBitmapRenderer.calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            while (outOfMemoryError) {
                try {
                    decodedBitmap = BitmapFactory.decodeResource(res, resId, options);

                    //Add bitmap to cache if bitmap was successfully rendered and cache is available
                    if (decodedBitmap != null && epicBitmapCache != null) {
                        epicBitmapCache.put(String.valueOf(resId), decodedBitmap, options.outMimeType, 100);
                    }

                    outOfMemoryError = false;
                } catch (OutOfMemoryError e) {
                    //If inSampleSize still not enough to avoid out of memory error, increase it
                    options.inSampleSize *= 2;
                    outOfMemoryError = true;
                }

                if (options.inSampleSize >= 20) {
                    //Break loop in case of too many loops (something else is happening)
                    outOfMemoryError = false;
                }
            }
        }

        return decodedBitmap;
    }

    /**
     * <p>
     * Decodes a sampled {@link Bitmap} object from a given app resource, using the inSampleSize specified.
     * </p>
     * <p>
     * <b>Important Note:</b> This method is synchronous and can cause UI Thread to freeze, use
     * {@link #decodeBitmapFromResource(Resources, int, int, OnBitmapRendered, OnBitmapRenderFailed)} instead for an asynchronous solution.
     * </p>
     *
     * @param res          Resources package. You can get default resources package using {@link Activity#getResources()} inside an activity or {@link Context#getResources()} outside if a {@link Context} is available.
     * @param resId        App resource id. Could be either the pure integer value, or the Android resource name (R.drawable.img_name).
     * @param inSampleSize Determines how many times image resolution is divided to lower memory usage. Image aspect ratio is not affected by this parameter, just its resolution / quality is lowered.
     * @return Decoded {@link Bitmap} object, ready to use on any View or code.
     */
    public static Bitmap decodeBitmapFromResource(Resources res, int resId, int inSampleSize) {
        Bitmap decodedBitmap = null;

        // Search bitmap on cache first if available
        if (epicBitmapCache != null) {
            decodedBitmap = epicBitmapCache.getBitmapFromCache(String.valueOf(resId));
        }

        //If bitmap not found on cache, render it
        if (decodedBitmap == null) {
            // Decode bitmap with inSampleSize set
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = inSampleSize;
            decodedBitmap = BitmapFactory.decodeResource(res, resId, options);

            //Add bitmap to cache if bitmap was successfully rendered and cache is available
            if (decodedBitmap != null && epicBitmapCache != null) {
                epicBitmapCache.put(String.valueOf(resId), decodedBitmap, options.outMimeType, 100);
            }
        }

        return decodedBitmap;
    }

    /**
     * <p>
     * Decodes a {@link Bitmap} object from a given app resource, at its original dimensions.
     * </p>
     * <p>
     * <b>Important Note:</b> This method is synchronous and can cause UI Thread to freeze,
     * use {@link #decodeBitmapFromResource(Resources, int, OnBitmapRendered, OnBitmapRenderFailed)} instead for an asynchronous solution.
     * </p>
     *
     * @param res   Resources package. You can get default resources package using {@link Activity#getResources()} inside an activity or {@link Context#getResources()} outside if a {@link Context} is available.
     * @param resId App resource id. Could be either the pure integer value, or the Android resource name (R.drawable.img_name).
     * @return Decoded {@link Bitmap} object, ready to use on any View or code.
     */
    public static Bitmap decodeBitmapFromResource(Resources res, int resId) {
        return decodeBitmapFromResource(res, resId, 1);
    }

    /**
     * <p>
     * Decodes a {@link Bitmap} object from a given file, using the specified measures to calculate image downsample if needed.
     * Downsample rate is auto-increased if bitmap rendering causes an {@link OutOfMemoryError}.
     * </p>
     * <p>
     * <b>Important Note:</b> This method is synchronous and can cause UI Thread to freeze, use {@link #decodeBitmapFromFile(String, int, int, OnBitmapRendered, OnBitmapRenderFailed)}
     * instead for an asynchronous solution.
     * </p>
     *
     * @param path      Physical path of File in the device storage.
     * @param reqWidth  Required width of the view where the Bitmap should fit. This parameter doesn't affect image aspect ratio, it's only used to calculate the inSampleSize of the image in case a downsample is required.
     * @param reqHeight Required height of the view where the Bitmap should fit. This parameter doesn't affect image aspect ratio, it's only used to calculate the inSampleSize of the image in case a downsample is required.
     * @return Decoded {@link Bitmap} object, ready to use on any View or code.
     */
    public static Bitmap decodeBitmapFromFile(String path, int reqWidth, int reqHeight) {
        Bitmap decodedBitmap = null;
        Boolean outOfMemoryError = true;
        String pathEncoded = "";

        //File path encoding into percent format, to use it as cache identifier (required to create a valid file name for disk cache)
        try {
            pathEncoded = URLEncoder.encode(path, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
        }

        // Search bitmap on cache first if available
        if (!pathEncoded.isEmpty() && epicBitmapCache != null) {
            decodedBitmap = epicBitmapCache.getBitmapFromCache(pathEncoded);
        }

        //If bitmap not found on cache, render it
        if (decodedBitmap == null) {
            // First decode with inJustDecodeBounds=true (No memory allocation) to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            // Calculate inSampleSize
            options.inSampleSize = EpicBitmapRenderer.calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            while (outOfMemoryError) {
                try {
                    decodedBitmap = BitmapFactory.decodeFile(path, options);

                    //Add bitmap to cache if bitmap was successfully rendered and cache is available
                    if (!pathEncoded.isEmpty() && decodedBitmap != null && epicBitmapCache != null) {
                        epicBitmapCache.put(pathEncoded, decodedBitmap, options.outMimeType, 100);
                    }

                    outOfMemoryError = false;
                } catch (OutOfMemoryError e) {
                    //If inSampleSize still not enough to avoid out of memory error, increase it
                    options.inSampleSize *= 2;
                    outOfMemoryError = true;
                }

                if (options.inSampleSize >= 20) {
                    //Break loop in case of too many loops (something else is happening)
                    outOfMemoryError = false;
                }
            }
        }

        return decodedBitmap;
    }

    /**
     * <p>
     * Decodes a sampled {@link Bitmap} object from a given file, using the inSampleSize specified.
     * </p>
     * <p>
     * <b>Important Note:</b> This method is synchronous and can cause UI Thread to freeze,
     * use {@link #decodeBitmapFromFile(String, int, OnBitmapRendered, OnBitmapRenderFailed)} instead for an asynchronous solution.
     * </p>
     *
     * @param path         Physical path of File in the device storage.
     * @param inSampleSize Determines how many times image resolution is divided to lower memory usage. Image aspect ratio is not affected by this parameter, just its resolution / quality is lowered.
     * @return Decoded {@link Bitmap} object, ready to use on any View or code.
     */
    public static Bitmap decodeBitmapFromFile(String path, int inSampleSize) {
        Bitmap decodedBitmap = null;
        String pathEncoded = "";

        //File path encoding into percent format, to use it as cache identifier (required to create a valid file name for disk cache)
        try {
            pathEncoded = URLEncoder.encode(path, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
        }

        // Search bitmap on cache first if available
        if (!pathEncoded.isEmpty() && epicBitmapCache != null) {
            decodedBitmap = epicBitmapCache.getBitmapFromCache(pathEncoded);
        }

        //If bitmap not found on cache, render it
        if (decodedBitmap == null) {
            // Decode bitmap with inSampleSize set
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = inSampleSize;
            decodedBitmap = BitmapFactory.decodeFile(path, options);

            //Add bitmap to cache if bitmap was successfully rendered and cache is available
            if (!pathEncoded.isEmpty() && decodedBitmap != null && epicBitmapCache != null) {
                epicBitmapCache.put(pathEncoded, decodedBitmap, options.outMimeType, 100);
            }
        }

        return decodedBitmap;
    }

    /**
     * <p>
     * Decodes a {@link Bitmap} object from a given file, at its original dimensions.
     * </p>
     * <p>
     * <b>Important Note:</b> This method is synchronous and can cause UI Thread to freeze,
     * use {@link #decodeBitmapFromFile(String, OnBitmapRendered, OnBitmapRenderFailed)} instead for an asynchronous solution.
     * </p>
     *
     * @param path Physical path of File in the device storage.
     * @return Decoded {@link Bitmap} object, ready to use on any View or code.
     */
    public static Bitmap decodeBitmapFromFile(String path) {
        return decodeBitmapFromFile(path, 1);
    }
    //endregion Rendering Synchronous Methods

    //region Rendering Async methods

    /**
     * Decodes a sampled {@link Bitmap} object from a given app resource asynchronously, using the specified measures to calculate image downsample if needed.
     * Downsample rate is auto-increased if bitmap rendering causes an {@link OutOfMemoryError}.
     *
     * @param res                  Resources package. You can get default resources package using {@link Activity#getResources()} inside an activity or {@link Context#getResources()} outside if a {@link Context} is available.
     * @param resId                App resource id. Could be either the pure integer value, or the Android resource name (R.drawable.img_name).
     * @param reqWidth             Required width of the view where the Bitmap should fit. This parameter doesn't affect image aspect ratio, it's only used to calculate the inSampleSize of the image in case a downsample is required.
     * @param reqHeight            Required height of the view where the Bitmap should fit. This parameter doesn't affect image aspect ratio, it's only used to calculate the inSampleSize of the image in case a downsample is required.
     * @param onBitmapRendered     Overwrite this callback to retrieve {@link Bitmap} object rendered once it's ready and perform any other actions needed.
     * @param onBitmapRenderFailed Overwrite this callback to perform actions when {@link Bitmap} object fails to render. Can be null.
     */
    public static void decodeBitmapFromResource(Resources res, int resId,
                                                int reqWidth, int reqHeight,
                                                OnBitmapRendered onBitmapRendered,
                                                OnBitmapRenderFailed onBitmapRenderFailed) {
        //Launch renderer AsyncTask
        new AsyncDecodeResMeasured(res, resId, reqWidth, reqHeight, onBitmapRendered, onBitmapRenderFailed, epicBitmapCache).execute();
    }

    /**
     * Decodes a sampled {@link Bitmap} object from a given app resource asynchronously, using the inSampleSize specified.
     * Downsample rate is auto-increased if bitmap rendering causes an {@link OutOfMemoryError}.
     *
     * @param res                  Resources package. You can get default resources package using {@link Activity#getResources()} inside an activity or {@link Context#getResources()} outside if a {@link Context} is available.
     * @param resId                App resource id. Could be either the pure integer value, or the Android resource name (R.drawable.img_name).
     * @param inSampleSize         Determines how many times image resolution is divided to lower memory usage. Image aspect ratio is not affected by this parameter, just its resolution / quality is lowered.
     * @param onBitmapRendered     Overwrite this callback to retrieve {@link Bitmap} object rendered once it's ready and perform any other actions needed.
     * @param onBitmapRenderFailed Overwrite this callback to perform actions when {@link Bitmap} object fails to render. Can be null.
     */
    public static void decodeBitmapFromResource(Resources res, int resId,
                                                int inSampleSize,
                                                OnBitmapRendered onBitmapRendered,
                                                OnBitmapRenderFailed onBitmapRenderFailed) {
        //Launch renderer AsyncTask
        new AsyncDecodeResForced(res, resId, inSampleSize, onBitmapRendered, onBitmapRenderFailed, epicBitmapCache).execute();
    }

    /**
     * Decodes a {@link Bitmap} object from a given app resource asynchronously, at its original dimensions.
     * Downsample rate is auto-increased if bitmap rendering causes an {@link OutOfMemoryError}.
     *
     * @param res                  Resources package. You can get default resources package using {@link Activity#getResources()} inside an activity or {@link Context#getResources()} outside if a {@link Context} is available.
     * @param resId                App resource id. Could be either the pure integer value, or the Android resource name (R.drawable.img_name).
     * @param onBitmapRendered     Overwrite this callback to retrieve {@link Bitmap} object rendered once it's ready and perform any other actions needed.
     * @param onBitmapRenderFailed Overwrite this callback to perform actions when {@link Bitmap} object fails to render. Can be null.
     */
    public static void decodeBitmapFromResource(Resources res, int resId,
                                                OnBitmapRendered onBitmapRendered,
                                                OnBitmapRenderFailed onBitmapRenderFailed) {
        decodeBitmapFromResource(res, resId, 1, onBitmapRendered, onBitmapRenderFailed);
    }

    /**
     * <p>
     * Decodes a sampled {@link Bitmap} object from a given file asynchronously, using the specified measures to calculate image downsample if needed.
     * Downsample rate is auto-increased if bitmap rendering causes an {@link OutOfMemoryError}.
     * </p>
     * <p>
     * <b>Permissions:</b> If file is outside app's own folders, this method requires the app using the library to
     * get permission android.permissions.READ_EXTERNAL_STORAGE in order to work. If permission
     * android.permissions.WRITE_EXTERNAL_STORAGE is granted, READ_EXTERNAL_STORAGE permission is also granted automatically.
     * </p>
     *
     * @param path                 Physical path of File in the device storage.
     * @param reqWidth             Required width of the view where the Bitmap should fit. This parameter doesn't affect image aspect ratio, it's only used to calculate the inSampleSize of the image in case a downsample is required.
     * @param reqHeight            Required height of the view where the Bitmap should fit. This parameter doesn't affect image aspect ratio, it's only used to calculate the inSampleSize of the image in case a downsample is required.
     * @param onBitmapRendered     Overwrite this callback to retrieve {@link Bitmap} object rendered once it's ready and perform any other actions needed.
     * @param onBitmapRenderFailed Overwrite this callback to perform actions when {@link Bitmap} object fails to render. Can be null.
     */
    public static void decodeBitmapFromFile(String path, int reqWidth, int reqHeight,
                                            OnBitmapRendered onBitmapRendered,
                                            OnBitmapRenderFailed onBitmapRenderFailed) {
        //Launch renderer AsyncTask
        new AsyncDecodeFileMeasured(path, reqWidth, reqHeight, onBitmapRendered, onBitmapRenderFailed, epicBitmapCache).execute();
    }

    /**
     * <p>
     * Decodes a sampled {@link Bitmap} object from a given file asynchronously, using the inSampleSize specified.
     * Downsample rate is auto-increased if bitmap rendering causes an {@link OutOfMemoryError}.
     * </p>
     * <p>
     * <b>Permissions:</b> If file is outside app's own folders, this method requires the app using the library to
     * get permission android.permissions.READ_EXTERNAL_STORAGE in order to work. If permission
     * android.permissions.WRITE_EXTERNAL_STORAGE is granted, READ_EXTERNAL_STORAGE permission is also granted automatically.
     * </p>
     *
     * @param path                 Physical path of File in the device storage.
     * @param inSampleSize         Determines how many times image resolution is divided to lower memory usage. Image aspect ratio is not affected by this parameter, just its resolution / quality is lowered.
     * @param onBitmapRendered     Overwrite this callback to retrieve {@link Bitmap} object rendered once it's ready and perform any other actions needed.
     * @param onBitmapRenderFailed Overwrite this callback to perform actions when {@link Bitmap} object fails to render. Can be null.
     */
    public static void decodeBitmapFromFile(String path, int inSampleSize,
                                            OnBitmapRendered onBitmapRendered,
                                            OnBitmapRenderFailed onBitmapRenderFailed) {
        new AsyncDecodeFileForced(path, inSampleSize, onBitmapRendered, onBitmapRenderFailed, epicBitmapCache).execute();
    }

    /**
     * <p>
     * Decodes a {@link Bitmap} object from a given file asynchronously, at its original dimensions.
     * Downsample rate is auto-increased if bitmap rendering causes an {@link OutOfMemoryError}.
     * </p>
     * <p>
     * <b>Permissions:</b> If file is outside app's own folders, this method requires the app using the library to
     * get permission android.permissions.READ_EXTERNAL_STORAGE in order to work. If permission
     * android.permissions.WRITE_EXTERNAL_STORAGE is granted, READ_EXTERNAL_STORAGE permission is also granted automatically.
     * </p>
     *
     * @param path                 Physical path of File in the device storage.
     * @param onBitmapRendered     Overwrite this callback to retrieve {@link Bitmap} object rendered once it's ready and perform any other actions needed.
     * @param onBitmapRenderFailed Overwrite this callback to perform actions when {@link Bitmap} object fails to render. Can be null.
     */
    public static void decodeBitmapFromFile(String path,
                                            OnBitmapRendered onBitmapRendered,
                                            OnBitmapRenderFailed onBitmapRenderFailed) {
        decodeBitmapFromFile(path, 1, onBitmapRendered, onBitmapRenderFailed);
    }

    /**
     * <p>
     * Decodes a sampled {@link Bitmap} object from a given url, using the specified measures to calculate image downsample if needed.
     * </p>
     * <p><b>Permissions:</b> This method requires the app using the library to use permission android.permissions.INTERNET in order to work.</p>
     *
     * @param url                  Image resource URL (e.g. http://www.website.com/image.png)
     * @param reqWidth             Required width of the view where the Bitmap should fit. This parameter doesn't affect image aspect ratio, it's only used to calculate the inSampleSize of the image in case a downsample is required.
     * @param reqHeight            Required height of the view where the Bitmap should fit. This parameter doesn't affect image aspect ratio, it's only used to calculate the inSampleSize of the image in case a downsample is required.
     * @param onBitmapRendered     Overwrite this callback to retrieve {@link Bitmap} object rendered once it's ready and perform any other actions needed.
     * @param onBitmapRenderFailed Overwrite this callback to perform actions when {@link Bitmap} object fails to render. Can be null.
     */
    public static void decodeBitmapFromUrl(String url, int reqWidth, int reqHeight,
                                           OnBitmapRendered onBitmapRendered,
                                           OnBitmapRenderFailed onBitmapRenderFailed) {
        new AsyncDecodeUrlMeasured(url, reqWidth, reqHeight, onBitmapRendered, onBitmapRenderFailed, epicBitmapCache).execute();
    }

    /**
     * <p>
     * Decodes a sampled {@link Bitmap} object from a given url, using the inSampleSize specified.
     * </p>
     * <p><b>Permissions:</b> This method requires the app using the library to use permission android.permissions.INTERNET in order to work.</p>
     *
     * @param url                  Image resource URL (e.g. http://www.website.com/image.png)
     * @param inSampleSize         Determines how many times image resolution is divided to lower memory usage. Image aspect ratio is not affected by this parameter, just its resolution / quality is lowered.
     * @param onBitmapRendered     Overwrite this callback to retrieve {@link Bitmap} object rendered once it's ready and perform any other actions needed.
     * @param onBitmapRenderFailed Overwrite this callback to perform actions when {@link Bitmap} object fails to render. Can be null.
     */
    public static void decodeBitmapFromUrl(String url, int inSampleSize,
                                           OnBitmapRendered onBitmapRendered,
                                           OnBitmapRenderFailed onBitmapRenderFailed) {
        new AsyncDecodeUrlForced(url, inSampleSize, onBitmapRendered, onBitmapRenderFailed, epicBitmapCache).execute();
    }

    /**
     * <p>
     * Decodes a {@link Bitmap} object from a given url, at its original dimensions.
     * </p>
     * <p><b>Permissions:</b> This method requires the app using the library to use permission android.permissions.INTERNET in order to work.</p>
     *
     * @param url                  Image resource URL (e.g. http://www.website.com/image.png)
     * @param onBitmapRendered     Overwrite this callback to retrieve {@link Bitmap} object rendered once it's ready and perform any other actions needed.
     * @param onBitmapRenderFailed Overwrite this callback to perform actions when {@link Bitmap} object fails to render. Can be null.
     */
    public static void decodeBitmapFromUrl(String url,
                                           OnBitmapRendered onBitmapRendered,
                                           OnBitmapRenderFailed onBitmapRenderFailed) {
        decodeBitmapFromUrl(url, 1, onBitmapRendered, onBitmapRenderFailed);
    }
    //endregion Rendering Async methods

    //region Cache methods

    //TODO: Find a way to initialize disk cache automatically (needs application context), without asking the user to call a method (initDiskCache) passing Context as parameter. Context is just used to retrieve app's cache dir

    /**
     * <p>Initializes disk caches.</p>
     * <p>
     * <b>Important Note:</b> Until an alternative is found, this method must be called (Just once for whole app life cycle, and in any time)
     * before using any render method, other way, {@link Bitmap} objects rendered won't be stored on disk cache, just in memory cache.
     * </p>
     *
     * @param context {@link Context} from where lib is being called. This is used to get application's cache dir for disk cache.
     */
    public static void initDiskCache(Context context) {
        epicBitmapCache.initDiskCache(context);
    }
    //endregion Cache methods

    //region Helper methods

    /**
     * Calculates downsample rate, if needed, for an image depending of width and height it should fit on.
     *
     * @param options   {@link android.graphics.BitmapFactory.Options} object containing image info.
     * @param reqWidth  Required width of the view where the Bitmap should fit.
     * @param reqHeight Required height of the view where the Bitmap should fit.
     * @return int representing inSampleSize, a.k.a. the number of times image resolution is divided to lower memory usage.
     * @see <a href="https://developer.android.com/training/displaying-bitmaps/load-bitmap.html#load-bitmap">Google conventions for calculate inSampleSize.</a>
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if ((reqHeight > 0 || reqWidth > 0) && (height > reqHeight || width > reqWidth)) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    //endregion Helper methods
}
