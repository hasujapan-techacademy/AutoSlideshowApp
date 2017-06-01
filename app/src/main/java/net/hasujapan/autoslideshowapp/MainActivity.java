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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ViewFlipper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AutoSlideshowApp";

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int IMAGE_FLIP_INTERVAL = 2000;

    private ViewFlipper mImageViewFlipper;

    // -----------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initImageViewFlipper();
        loadImages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetImageViewFlipper();
    }

    // -----------------------------------------------------------------------

    private void initImageViewFlipper() {
        mImageViewFlipper = (ViewFlipper) findViewById(R.id.imageViewFlipper);
        mImageViewFlipper.setFlipInterval(IMAGE_FLIP_INTERVAL);
    }

    private void resetImageViewFlipper() {
        if(mImageViewFlipper.getVisibility() == View.VISIBLE) {
            mImageViewFlipper.removeAllViews();
            loadImagesFromGallery(); // スライドショーが有効な状態になっていたら画像を最新の状態に更新する
        }
    }

    // -----------------------------------------------------------------------

    private void enableSlideshow() {
        mImageViewFlipper.setVisibility(View.VISIBLE);
        setPrevButtonListener();
        setNextButtonListener();
        setToggleAutoPlayButtonListener();
        loadImagesFromGallery();
    }

    // -----------------------------------------------------------------------

    private void setAllowMediaAccessButton() {
        Button allowMediaAccessButton = (Button) findViewById(R.id.allowMediaAccessButton);
        allowMediaAccessButton.setVisibility(View.VISIBLE);
        allowMediaAccessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNeedConfirmationForPermission()) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
                }
            }
        });
    }

    private void setPrevButtonListener() {
        Button prevButton = (Button) findViewById(R.id.prevButton);
        prevButton.setVisibility(View.VISIBLE);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageViewFlipper.showPrevious();
            }
        });
    }

    private void setNextButtonListener() {
        Button nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setVisibility(View.VISIBLE);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageViewFlipper.showNext();
            }
        });
    }

    private void setToggleAutoPlayButtonListener() {
        Button toggleAutoPlayButton = (Button) findViewById(R.id.toggleAutoPlayButton);
        toggleAutoPlayButton.setVisibility(View.VISIBLE);
        toggleAutoPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mImageViewFlipper.isFlipping()) {
                    stopImageFlip();
                }
                else {
                    startImageFlip();
                }
            }
        });
    }

    // -----------------------------------------------------------------------

    private void disableFlipButtons() {
        Button prevButton = (Button) findViewById(R.id.prevButton);
        Button nextButton = (Button) findViewById(R.id.nextButton);
        prevButton.setEnabled(false);
        nextButton.setEnabled(false);
    }

    private void enableFlipButtons() {
        Button prevButton = (Button) findViewById(R.id.prevButton);
        Button nextButton = (Button) findViewById(R.id.nextButton);
        prevButton.setEnabled(true);
        nextButton.setEnabled(true);
    }

    private void startImageFlip() {
        Button toggleAutoPlayButton = (Button) findViewById(R.id.toggleAutoPlayButton);
        toggleAutoPlayButton.setText("STOP");
        disableFlipButtons();
        mImageViewFlipper.startFlipping();
    }

    private void stopImageFlip() {
        Button toggleAutoPlayButton = (Button) findViewById(R.id.toggleAutoPlayButton);
        toggleAutoPlayButton.setText("START");
        enableFlipButtons();
        mImageViewFlipper.stopFlipping();
    }

    // -----------------------------------------------------------------------

    // ストレージアクセスのパーミッションをチェック + スライドショーの開始

    private void loadImages() {
        if(isNeedConfirmationForPermission() && ! hasExternalStorageAccessPermission()) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
        }
        else {
            enableSlideshow();
        }
    }

    // 端末から取得した画像をスライドショーに読み込む

    private void loadImagesFromGallery() {

        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setImageURI(imageUri);
                mImageViewFlipper.addView(imageView); // 全画像を ViewFlipper に入れる（実機だと枚数が多い場合メモリ不足でたぶん落ちる）
            } while (cursor.moveToNext());
        }
        cursor.close(); // 画像が一枚も無かった場合の処理は省略（）
    }

    // ストレージへのアクセス許可の確認が必要な端末かどうかを判定する

    private boolean isNeedConfirmationForPermission() {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            result = true;
        }
        return result;
    }

    // ストレージへのアクセス許可を許可されているかどうかを判定する

    private boolean hasExternalStorageAccessPermission() {
        boolean result = false;
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            result = true;
        }
        return result;
    }

    // -----------------------------------------------------------------------

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Button allowMediaAccessButton = (Button) findViewById(R.id.allowMediaAccessButton);
                    allowMediaAccessButton.setVisibility(View.INVISIBLE);
                    enableSlideshow();
                }
                else {
                    setAllowMediaAccessButton();
                }
                break;
            default:
                break;
        }
    }

    // -----------------------------------------------------------------------
}
