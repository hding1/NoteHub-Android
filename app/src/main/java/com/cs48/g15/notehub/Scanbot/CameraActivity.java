package com.cs48.g15.notehub.Scanbot;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.support.v4.view.WindowCompat;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.cs48.g15.notehub.R;

import net.doo.snap.camera.AutoSnappingController;
import net.doo.snap.camera.CameraOpenCallback;
import net.doo.snap.camera.ContourDetectorFrameHandler;
import net.doo.snap.camera.PictureCallback;
import net.doo.snap.camera.ScanbotCameraView;
import net.doo.snap.lib.detector.ContourDetector;
import net.doo.snap.lib.detector.DetectionResult;
import net.doo.snap.ui.PolygonView;
import net.doo.snap.lib.detector.ContourDetector;
import net.doo.snap.lib.detector.DetectionResult;
import net.doo.snap.lib.detector.Line2D;
import net.doo.snap.ui.EditPolygonImageView;
import net.doo.snap.ui.MagnifierView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity implements PictureCallback,
        ContourDetectorFrameHandler. ResultHandler{
//    @Override
//    public void onResume() {
//        super.onResume();
//        scanbotCameraView.onResume();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        scanbotCameraView.onPause();
//    }

    private ScanbotCameraView cameraView;
    private PolygonView polygonView;
    private ImageView resultView;
    private ContourDetectorFrameHandler contourDetectorFrameHandler;
    private AutoSnappingController autoSnappingController;
    private Toast userGuidanceToast;

    private boolean flashEnabled = false;
    private boolean autoSnappingEnabled = true;

    //private String savedPath;
    private String user;

    private Bitmap savedBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);
        Intent intent = getIntent();
        user = intent.getStringExtra("username");

        //getSupportActionBar().hide();

        cameraView = (ScanbotCameraView) findViewById(R.id.camera);
        cameraView.setCameraOpenCallback(new CameraOpenCallback() {
            @Override
            public void onCameraOpened() {

                cameraView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cameraView.setAutoFocusSound(false);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            cameraView.setShutterSound(false);
                        }

                        cameraView.continuousFocus();
                        cameraView.useFlash(flashEnabled);
                    }
                }, 700);
            }
        });

        resultView = (ImageView) findViewById(R.id.result);

        contourDetectorFrameHandler = ContourDetectorFrameHandler.attach(cameraView);

        // Please note: https://github.com/doo/Scanbot-SDK-Examples/wiki/Detecting-and-drawing-contours#contour-detection-parameters
        contourDetectorFrameHandler.setAcceptedAngleScore(75);
        contourDetectorFrameHandler.setAcceptedSizeScore(80);

        polygonView = (PolygonView) findViewById(R.id.polygonView);
        contourDetectorFrameHandler.addResultHandler(polygonView);
        contourDetectorFrameHandler.addResultHandler(this);

        autoSnappingController = AutoSnappingController.attach(cameraView, contourDetectorFrameHandler);

        cameraView.addPictureCallback(this);

        userGuidanceToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        userGuidanceToast.setGravity(Gravity.CENTER, 0, 0);

        findViewById(R.id.snap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.takePicture(false);
            }
        });

        findViewById(R.id.flash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flashEnabled = !flashEnabled;
                cameraView.useFlash(flashEnabled);
            }
        });

        findViewById(R.id.autoSnappingToggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoSnappingEnabled = !autoSnappingEnabled;
                setAutoSnapEnabled(autoSnappingEnabled);
            }
        });


        findViewById(R.id.result).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent EditPolygon = new Intent(CameraActivity.this, EditPolygonImageActivity.class);
                //ImageView imageView = (ImageView)this.findViewById(R.class.resultView);
                //Bitmap documentImage = ((BitmapDrawable)resultView.getDrawable()).getBitmap();

//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                savedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                byte[] bitmapByte = baos.toByteArray();

                Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), savedBitmap, null,null));

                EditPolygon.putExtra("pictureUri",uri.toString());
                EditPolygon.putExtra("username",user);
                startActivity(EditPolygon);
                finish();
                //update_file(uid, filename, tag);
                //upload(username, pathname, tag);
            }
        });

        setAutoSnapEnabled(autoSnappingEnabled);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();

        //cameraView.startPreview();
    }

    @Override
    public void onBackPressed(){
        super.finish();
    }


    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
    }

    @Override
    public boolean handleResult(final ContourDetectorFrameHandler.DetectedFrame detectedFrame) {
        // Here you are continuously notified about contour detection results.
        // For example, you can show a user guidance text depending on the current detection status.
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showUserGuidance(detectedFrame.detectionResult);
            }
        });

        return false; // typically you need to return false
    }

    private void showUserGuidance(final DetectionResult result) {
        if (!autoSnappingEnabled) {
            return;
        }

        switch (result) {
            case OK:
                userGuidanceToast.setText("Don't move");
                userGuidanceToast.show();
                break;
            case OK_BUT_TOO_SMALL:
                userGuidanceToast.setText("Move closer");
                userGuidanceToast.show();
                break;
            case OK_BUT_BAD_ANGLES:
                userGuidanceToast.setText("Perspective");
                userGuidanceToast.show();
                break;
            case ERROR_NOTHING_DETECTED:
                userGuidanceToast.setText("No Document");
                userGuidanceToast.show();
                break;
            case ERROR_TOO_NOISY:
                userGuidanceToast.setText("Background too noisy");
                userGuidanceToast.show();
                break;
            case ERROR_TOO_DARK:
                userGuidanceToast.setText("Poor light");
                userGuidanceToast.show();
                break;
            default:
                userGuidanceToast.cancel();
                break;
        }
    }

    @Override
    public void onPictureTaken(byte[] image, int imageOrientation) {
        // Here we get the full image from the camera.
        // Implement a suitable async(!) detection and image handling here.
        // This is just a demo showing detected image as downscaled preview image.

        // Decode Bitmap from bytes of original image:
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1; // use 1 for original size (if you want no downscale)!
        // in this demo we downscale the image to 1/8 for the preview.
        Bitmap originalBitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);
        //rotate original image if required:
        if (imageOrientation > 0) {
            final Matrix matrix = new Matrix();
            matrix.setRotate(imageOrientation, originalBitmap.getWidth() / 2f, originalBitmap.getHeight() / 2f);
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, false);
        }

        savedBitmap = originalBitmap;
        //savedPath = saveToInternalStorage(originalBitmap);


        options.inSampleSize = 8;
        originalBitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);
        if (imageOrientation > 0) {
            final Matrix matrix = new Matrix();
            matrix.setRotate(imageOrientation, originalBitmap.getWidth() / 2f, originalBitmap.getHeight() / 2f);
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, false);
        }

        // Run document detection on original image:
        final ContourDetector detector = new ContourDetector();
        detector.detect(originalBitmap);
        final Bitmap documentImage = detector.processImageAndRelease(originalBitmap, detector.getPolygonF(), ContourDetector.IMAGE_FILTER_NONE);

        resultView.post(new Runnable() {
            @Override
            public void run() {
                resultView.setImageBitmap(documentImage);
                cameraView.continuousFocus();
                cameraView.startPreview();
            }
        });
    }

    private void setAutoSnapEnabled(boolean enabled) {
        autoSnappingController.setEnabled(enabled);
        contourDetectorFrameHandler.setEnabled(enabled);
        polygonView.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"temp.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

}