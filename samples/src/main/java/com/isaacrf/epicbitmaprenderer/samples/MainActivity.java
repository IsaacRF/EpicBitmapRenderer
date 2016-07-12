package com.isaacrf.epicbitmaprenderer.samples;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.isaacrf.epicbitmaprenderer.core.EpicBitmapRenderer;
import com.isaacrf.epicbitmaprenderer.listeners.OnBitmapRenderFailed;
import com.isaacrf.epicbitmaprenderer.listeners.OnBitmapRendered;

public class MainActivity extends AppCompatActivity {
    private final int RESULT_RENDER_BITMAP_FROM_FILE = 1;
    private final int RESULT_REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: Need to find a way for library to automatically know the context of caller app (if possible) and initialize cache, avoiding to explicitly call initDiskCache() for Disk Cache to work
        //Initialize library's disk cache, needs context to find app's own cache directory
        EpicBitmapRenderer.initDiskCache(this);

        //Sample 1: Decode Bitmap from Resource (Async)
        EpicBitmapRenderer.decodeBitmapFromResource(getResources(), R.mipmap.ic_launcher, 200, 200,
                new OnBitmapRendered() {
                    @Override
                    public void onBitmapRendered(Bitmap bitmap) {
                        //Display rendered Bitmap when ready and hide loading animation
                        ((ImageView) findViewById(R.id.imgSampleDecodeResource)).setImageBitmap(bitmap);
                        findViewById(R.id.pbSampleDecodeResource).setVisibility(View.GONE);
                    }
                },
                new OnBitmapRenderFailed() {
                    @Override
                    public void onBitmapRenderFailed(Exception e) {
                        //Take actions if Bitmap fails to render
                        Toast.makeText(MainActivity.this, "Failed to load Bitmap from Resource", Toast.LENGTH_SHORT).show();
                        //Hide loading animation
                        findViewById(R.id.pbSampleDecodeResource).setVisibility(View.GONE);
                    }
                });

        //Sample 2: Decode Bitmap from File (Async)
        ((Button) findViewById(R.id.btnSampleDecodeFile)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show loading animation
                findViewById(R.id.pbSampleDecodeFile).setVisibility(View.VISIBLE);

                //Request READ_EXTERNAL_STORAGE permission. Permissions must be requested in runtime in Android 6.0 and higher
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show a permission requirement explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response!
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        alertDialogBuilder.setMessage("In order to decode a Bitmap from a File, app must have permissions to read external storage");
                        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // After the user sees the explanation,
                                // try again to request the permission.
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        RESULT_REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
                            }
                        });
                        alertDialogBuilder.create().show();
                    } else {
                        // No explanation needed, we can request the permission.

                        // RESULT_REQUEST_PERMISSION_READ_EXTERNAL_STORAGE is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                RESULT_REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
                    }
                } else {
                    //If permission granted, launch an intent to select image file from gallery and render it on onActivityResult
                    showImageSelector();
                }
            }
        });

        //Sample 3: Decode Bitmap from URL (Async)
        EpicBitmapRenderer.decodeBitmapFromUrl("http://isaacrf.com/wp-content/themes/Workality-Lite-child/images/IsaacRF.png", 200, 200,
                new OnBitmapRendered() {
                    @Override
                    public void onBitmapRendered(Bitmap bitmap) {
                        //Display rendered Bitmap when ready and hide loading animation
                        ((ImageView) findViewById(R.id.imgSampleDecodeUrl)).setImageBitmap(bitmap);
                        findViewById(R.id.pbSampleDecodeUrl).setVisibility(View.GONE);
                    }
                },
                new OnBitmapRenderFailed() {
                    @Override
                    public void onBitmapRenderFailed(Exception e) {
                        //Take actions if Bitmap fails to render
                        Toast.makeText(MainActivity.this, "Failed to load Bitmap from URL", Toast.LENGTH_SHORT).show();
                        //Hide loading animation
                        findViewById(R.id.pbSampleDecodeUrl).setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            //File selected from Sample 2, render it
            case RESULT_RENDER_BITMAP_FROM_FILE:
                if (data != null) {
                    //String path = getRealPathFromURI(this, data.getData());
                    String path = getPath(this, data.getData());

                    EpicBitmapRenderer.decodeBitmapFromFile(path, 200, 200,
                            new OnBitmapRendered() {
                                @Override
                                public void onBitmapRendered(Bitmap bitmap) {
                                    //Display rendered Bitmap when ready and hide loading animation
                                    ((ImageView) findViewById(R.id.imgSampleDecodeFile)).setImageBitmap(bitmap);
                                    findViewById(R.id.pbSampleDecodeFile).setVisibility(View.GONE);
                                }
                            },
                            new OnBitmapRenderFailed() {
                                @Override
                                public void onBitmapRenderFailed(Exception e) {
                                    //Take actions if Bitmap fails to render
                                    Toast.makeText(MainActivity.this, "Failed to load Bitmap from Resource", Toast.LENGTH_SHORT).show();
                                    //Hide loading animation
                                    findViewById(R.id.pbSampleDecodeFile).setVisibility(View.GONE);
                                }
                            });
                } else {
                    //Hide loading animation
                    findViewById(R.id.pbSampleDecodeFile).setVisibility(View.GONE);
                }

                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RESULT_REQUEST_PERMISSION_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //If permission granted, launch an intent to select image file from gallery and render it on onActivityResult
                    showImageSelector();
                } else {
                    //Otherwise, hide loading animation
                    findViewById(R.id.pbSampleDecodeFile).setVisibility(View.GONE);
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * Launch an intent to select image file from gallery and render it on onActivityResult
     */
    public void showImageSelector() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_RENDER_BITMAP_FROM_FILE);
    }

    /**
     * Helper method to get real path from URI
     *
     * @param context Context
     * @return String representing real file path
     */
    private String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        String result = uri + "";
        // DocumentProvider
        //  if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
        if (isKitKat && (result.contains("media.documents"))) {
            String[] ary = result.split("/");
            int length = ary.length;
            String imgary = ary[length - 1];
            final String[] dat = imgary.split("%3A");
            final String docId = dat[1];
            final String type = dat[0];
            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
            } else if ("audio".equals(type)) {
            }
            final String selection = "_id=?";
            final String[] selectionArgs = new String[]{
                    dat[1]
            };
            return getDataColumn(context, contentUri, selection, selectionArgs);
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Helper method to get data column info from URI
     *
     * @param context       Context
     * @param uri           Uri to extrac data column from
     * @param selection     Selection
     * @param selectionArgs Selection Arguments
     * @return String with data from data column
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
}
