package net.hasujapan.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AutoSlideshowApp";

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    private static ArrayList<String> mImagePathList = new ArrayList<String>();

    // -----------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getImages();
    }

    // -----------------------------------------------------------------------

    private void getImages() {
        if(isNeedConfirmationForPermission() && ! hasExternalStorageAccessPermission()) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
        }
        else {
            getImagesFromGallery();
        }

    }

    private void getImagesFromGallery() {
        Log.d(TAG, "getImagesFromGallery()");

        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                mImagePathList.add(imageUri.toString());
                Log.d(TAG, "imageUri : " + imageUri.toString());
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
    
    // -----------------------------------------------------------------------

    private boolean isNeedConfirmationForPermission() {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            result = true;
        }
        Log.d(TAG, "isNeedConfirmationForPermission(): " + result);
        return result;
    }

    private boolean hasExternalStorageAccessPermission() {
        boolean result = false;
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            result = true;
        }
        Log.d(TAG, "hasExternalStorageAccessPermission(): " + result);
        return result;
    }

    // -----------------------------------------------------------------------
}
