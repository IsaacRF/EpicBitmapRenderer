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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.isaacrf.epicbitmaprenderer.core.EpicBitmapCache;
import com.isaacrf.epicbitmaprenderer.listeners.OnBitmapRenderFailed;
import com.isaacrf.epicbitmaprenderer.listeners.OnBitmapRendered;

/**
 * AsyncTask to decode a Bitmap from resource given its resource ID. InSampleSize parameter is forced to the value specified.
 */
public class AsyncDecodeResForced extends AsyncTask<Void, Void, Bitmap> {
    //region Fields
    private Resources resources;
    private int resourceId;
    private int inSampleSize;
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
     * @param res                  Resources package. You can get default resources package using {@link Activity#getResources()} inside an activity or {@link Context#getResources()} outside if a {@link Context} is available.
     * @param resId                App resource id. Could be either the pure integer value, or the Android resource name (R.drawable.img_name).
     * @param inSampleSize         Determines how many times image resolution is divided to lower memory usage. Image aspect ratio is not affected by this parameter, just its resolution / quality is lowered.
     * @param onBitmapRendered     Overwrite this callback to retrieve {@link Bitmap} object rendered once it's ready and perform any other actions needed.
     * @param onBitmapRenderFailed Overwrite this callback to perform actions when {@link Bitmap} object fails to render. Can be null.
     * @param epicBitmapCache      Cache to check if bitmap has already been rendered.
     */
    public AsyncDecodeResForced(Resources res, int resId,
                                int inSampleSize,
                                OnBitmapRendered onBitmapRendered,
                                OnBitmapRenderFailed onBitmapRenderFailed,
                                EpicBitmapCache epicBitmapCache) {
        this.resources = res;
        this.resourceId = resId;
        this.inSampleSize = inSampleSize;
        this.onBitmapRendered = onBitmapRendered;
        this.onBitmapRenderFailed = onBitmapRenderFailed;
        this.epicBitmapCache = epicBitmapCache;
    }

    /**
     * Basic constructor with just the required parameters.
     *
     * @param res              Resources package. You can get default resources package using {@link Activity#getResources()} inside an activity or {@link Context#getResources()} outside if a {@link Context} is available.
     * @param resId            App resource id. Could be either the pure integer value, or the Android resource name (R.drawable.img_name).
     * @param inSampleSize     Determines how many times image resolution is divided to lower memory usage. Image aspect ratio is not affected by this parameter, just its resolution / quality is lowered.
     * @param onBitmapRendered Overwrite this callback to retrieve {@link Bitmap} object rendered once it's ready and perform any other actions needed.
     */
    public AsyncDecodeResForced(Resources res, int resId,
                                int inSampleSize,
                                OnBitmapRendered onBitmapRendered) {
        this.resources = res;
        this.resourceId = resId;
        this.inSampleSize = inSampleSize;
        this.onBitmapRendered = onBitmapRendered;
        this.onBitmapRenderFailed = null;
        this.epicBitmapCache = null;
    }
    //endregion Constructors

    @Override
    protected Bitmap doInBackground(Void... params) {
        Bitmap decodedBitmap = null;

        // Search bitmap on cache first if available
        if (epicBitmapCache != null) {
            decodedBitmap = epicBitmapCache.getBitmapFromCache(String.valueOf(resourceId));
        }

        //If bitmap not found on cache, render it
        if (decodedBitmap == null) {
            // Decode bitmap with inSampleSize set
            try {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = inSampleSize;
                decodedBitmap = BitmapFactory.decodeResource(resources, resourceId, options);

                //Add bitmap to cache if bitmap was successfully rendered and cache is available
                if (decodedBitmap != null && epicBitmapCache != null) {
                    epicBitmapCache.put(String.valueOf(resourceId), decodedBitmap, options.outMimeType, 100);
                }
            } catch (Exception e) {
                //Set failException for later launch fail callback on main thread
                failException = e;
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
}
