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

package com.isaacrf.epicbitmaprenderer.asynctasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.isaacrf.epicbitmaprenderer.core.EpicBitmapCache;
import com.isaacrf.epicbitmaprenderer.core.EpicBitmapRenderer;
import com.isaacrf.epicbitmaprenderer.listeners.OnBitmapRenderFailed;
import com.isaacrf.epicbitmaprenderer.listeners.OnBitmapRendered;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * AsyncTask to decode a Bitmap from a url given its desired dimensions.
 */
public class AsyncDecodeUrlMeasured extends AsyncTask<Void, Void, Bitmap> {
    //region Fields
    private String url;
    private int requiredWidth;
    private int requiredHeight;
    private Exception failException;
    private EpicBitmapCache epicBitmapCache;
    //endregion Fields

    //region Listeners
    private OnBitmapRendered onBitmapRendered;
    private OnBitmapRenderFailed onBitmapRenderFailed;
    //endregion Listeners

    //region Constructors

    /**
     * All parameters constructor.
     *
     * @param url                  Image resource URL (e.g. http://www.website.com/image.png)
     * @param reqWidth             Required width of the view where the Bitmap should fit. This parameter doesn't affect image aspect ratio, it's only used to calculate the inSampleSize of the image in case a downsample is required.
     * @param reqHeight            Required height of the view where the Bitmap should fit. This parameter doesn't affect image aspect ratio, it's only used to calculate the inSampleSize of the image in case a downsample is required.
     * @param onBitmapRendered     Overwrite this callback to retrieve {@link Bitmap} object rendered once it's ready and perform any other actions needed.
     * @param onBitmapRenderFailed Overwrite this callback to perform actions when {@link Bitmap} object fails to render. Can be null.
     * @param epicBitmapCache      Cache to check if bitmap has already been rendered.
     */
    public AsyncDecodeUrlMeasured(String url, int reqWidth, int reqHeight,
                                  OnBitmapRendered onBitmapRendered,
                                  OnBitmapRenderFailed onBitmapRenderFailed,
                                  EpicBitmapCache epicBitmapCache) {
        this.url = url;
        this.requiredWidth = reqWidth;
        this.requiredHeight = reqHeight;
        this.onBitmapRendered = onBitmapRendered;
        this.onBitmapRenderFailed = onBitmapRenderFailed;
        this.epicBitmapCache = epicBitmapCache;
    }

    /**
     * Basic constructor with just the required parameters.
     *
     * @param url              Image resource URL (e.g. http://www.website.com/image.png)
     * @param reqWidth         Required width of the view where the Bitmap should fit. This parameter doesn't affect image aspect ratio, it's only used to calculate the inSampleSize of the image in case a downsample is required.
     * @param reqHeight        Required height of the view where the Bitmap should fit. This parameter doesn't affect image aspect ratio, it's only used to calculate the inSampleSize of the image in case a downsample is required.
     * @param onBitmapRendered Overwrite this callback to retrieve {@link Bitmap} object rendered once it's ready and perform any other actions needed.
     */
    public AsyncDecodeUrlMeasured(String url, int reqWidth, int reqHeight,
                                  OnBitmapRendered onBitmapRendered) {
        this.url = url;
        this.requiredWidth = reqWidth;
        this.requiredHeight = reqHeight;
        this.onBitmapRendered = onBitmapRendered;
        this.onBitmapRenderFailed = null;
        this.epicBitmapCache = null;
    }
    //endregion Constructors

    @Override
    protected Bitmap doInBackground(Void... params) {
        Bitmap decodedBitmap = null;
        Boolean outOfMemoryError = true;
        InputStream urlInputStream = null;
        String urlEncoded = "";

        //URL encoding into percent format, to use it as cache identifier (required to create a valid file name for disk cache)
        try {
            urlEncoded = URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
        }

        // Search bitmap on cache first if available
        if (!urlEncoded.isEmpty() && epicBitmapCache != null) {
            decodedBitmap = epicBitmapCache.getBitmapFromCache(urlEncoded);
        }

        //If bitmap not found on cache, render it
        if (decodedBitmap == null) {
            try {
                //Open connection to Url
                urlInputStream = getUrlConnectionInputStream(url);

                if (urlInputStream != null) {
                    // First decode with inJustDecodeBounds=true (No memory allocation) to check dimensions
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(urlInputStream, null, options);

                    // Calculate inSampleSize
                    options.inSampleSize = EpicBitmapRenderer.calculateInSampleSize(options, requiredWidth, requiredHeight);

                    // Decode bitmap with inSampleSize set
                    options.inJustDecodeBounds = false;
                    //Reset connection, other way decodeStream will return null
                    urlInputStream.close();
                    urlInputStream = getUrlConnectionInputStream(url);

                    if (urlInputStream != null) {
                        while (outOfMemoryError) {
                            try {
                                decodedBitmap = BitmapFactory.decodeStream(urlInputStream, null, options);

                                //Add bitmap to cache if bitmap was successfully rendered and cache is available
                                if (!urlEncoded.isEmpty() && decodedBitmap != null && epicBitmapCache != null) {
                                    epicBitmapCache.put(urlEncoded, decodedBitmap, options.outMimeType, 100);
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
                }
            } catch (Exception e) {
                //Set failException for later launch fail callback on main thread
                failException = e;
            } finally {
                //Close Input Stream
                if (urlInputStream != null) {
                    try {
                        urlInputStream.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }

        return decodedBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null) {
            if (onBitmapRendered != null) {
                //Call listener to return rendered bitmap
                onBitmapRendered.onBitmapRendered(bitmap);
            }
        } else if (onBitmapRenderFailed != null && failException != null) {
            //Call fail listener and send failException triggered
            onBitmapRenderFailed.onBitmapRenderFailed(failException);
        }
    }

    /**
     * Gets an {@link InputStream} from a given Url
     *
     * @param url Url from which InputStream should be obtained
     * @return {@link InputStream} object connected to Url
     */
    protected InputStream getUrlConnectionInputStream(String url) {
        URL urlFeed;
        URLConnection urlConnection;
        InputStream inputStream = null;

        try {
            urlFeed = new URL(url);
            urlConnection = urlFeed.openConnection();
            urlConnection.setUseCaches(true);

            inputStream = urlConnection.getInputStream();
        } catch (Exception e) {
            //Set failException for later launch fail callback on main thread
            failException = e;
        }

        return inputStream;
    }
}
