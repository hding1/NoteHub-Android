package com.cs48.g15.notehub.Scanbot;

import android.app.Dialog;
import android.content.ContextWrapper;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.view.WindowCompat;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.content.Intent;

import com.cs48.g15.notehub.R;

import net.doo.snap.camera.AutoSnappingController;
import net.doo.snap.camera.CameraOpenCallback;
import net.doo.snap.camera.ContourDetectorFrameHandler;
import net.doo.snap.camera.PictureCallback;
import net.doo.snap.camera.ScanbotCameraView;
import net.doo.snap.lib.detector.ContourDetector;
import net.doo.snap.lib.detector.DetectionResult;
import net.doo.snap.lib.detector.Line2D;
import net.doo.snap.ui.EditPolygonImageView;
import net.doo.snap.ui.MagnifierView;
import net.doo.snap.ui.PolygonView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
//import com.itextpdf.text.Document;
//import com.itextpdf.text.Image;
//import com.itextpdf.text.pdf.PdfWriter;

public class EditPolygonImageActivity extends AppCompatActivity {

    private EditPolygonImageView editPolygonView;
    private MagnifierView magnifierView;
    private Bitmap originalBitmap;
    private ImageView resultImageView;
    private Button cropButton, backCameraButton, backButton, saveButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_polygon_image);

        //getSupportActionBar().hide();

        editPolygonView = (EditPolygonImageView) findViewById(R.id.polygonView);

        Intent intent = getIntent();
        String path = intent.getStringExtra("Path");

        loadImageFromStorage(path);

//        try {
//            File f = new File(path,"temp,jpg");
//            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
//            editPolygonView.setImageBitmap(b);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }


        //BitmapDrawable p = new BitmapDrawable(getResources(),bitmap);

        //在这里要change source，真实情况应该是存在storage的某个地址
        //editPolygonView.setImageBitmap(b);
        originalBitmap = ((BitmapDrawable) editPolygonView.getDrawable()).getBitmap();

        magnifierView = (MagnifierView) findViewById(R.id.magnifier);
        // MagifierView should be set up every time when editPolygonView is set with new image
        magnifierView.setupMagnifier(editPolygonView);

        resultImageView = (ImageView) findViewById(R.id.resultImageView);
        resultImageView.setVisibility(View.GONE);

        cropButton = (Button) findViewById(R.id.cropButton);
        saveButton = (Button) findViewById(R.id.saveButton);

        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crop();
            }
        });

        backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backButton.setVisibility(View.GONE);
                saveButton.setVisibility(View.GONE);
                resultImageView.setVisibility(View.GONE);

                editPolygonView.setVisibility(View.VISIBLE);
                cropButton.setVisibility(View.VISIBLE);
            }
        });

        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bit;
                bit = originalBitmap = ((BitmapDrawable) resultImageView.getDrawable()).getBitmap();
                String saved = saveToExternalStorage(bit);

                // Input file
                String inputPath = saved;

                // Output file
                String outputPath = Environment.getExternalStorageDirectory() + File.separator + "out.pdf";
                convertToPdf(inputPath,outputPath);

                //println(t);
            }
        });

        new InitImageViewTask().executeOnExecutor(Executors.newSingleThreadExecutor(), originalBitmap);
    }

    private void crop() {
        // crop & warp image by selected polygon (editPolygonView.getPolygon())
        final Bitmap documentImage = new ContourDetector().processImageF(
                originalBitmap, editPolygonView.getPolygon(), ContourDetector.IMAGE_FILTER_NONE);

        editPolygonView.setVisibility(View.GONE);
        cropButton.setVisibility(View.GONE);

        resultImageView.setImageBitmap(documentImage);
        resultImageView.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.VISIBLE);

    }

    /**
     * Detects horizontal and vertical lines and polygon of the given bitmap image.
     * Initializes EditPolygonImageView with detected lines and polygon.
     */
    class InitImageViewTask extends AsyncTask<Bitmap, Void, InitImageResult> {

        @Override
        protected InitImageResult doInBackground(Bitmap... params) {
            Bitmap image = params[0];
            ContourDetector detector = new ContourDetector();
            final DetectionResult detectionResult = detector.detect(image);
            Pair<List<Line2D>, List<Line2D>> linesPair = null;
            List<PointF> polygon = new ArrayList<>(EditPolygonImageView.DEFAULT_POLYGON);
            switch (detectionResult) {
                case OK:
                case OK_BUT_BAD_ANGLES:
                case OK_BUT_TOO_SMALL:
                case OK_BUT_BAD_ASPECT_RATIO:
                    linesPair = new Pair<>(detector.getHorizontalLines(), detector.getVerticalLines());
                    polygon = detector.getPolygonF();
                    break;
            }

            return new InitImageResult(linesPair, polygon);
        }

        @Override
        protected void onPostExecute(final InitImageResult initImageResult) {
            // set detected polygon and lines into EditPolygonImageView
            editPolygonView.setPolygon(initImageResult.polygon);
            if (initImageResult.linesPair != null) {
                editPolygonView.setLines(initImageResult.linesPair.first, initImageResult.linesPair.second);
            }
        }
    }

    class InitImageResult {
        final Pair<List<Line2D>, List<Line2D>> linesPair;
        final List<PointF> polygon;

        InitImageResult(final Pair<List<Line2D>, List<Line2D>> linesPair, final List<PointF> polygon) {
            this.linesPair = linesPair;
            this.polygon = polygon;
        }
    }

    private void loadImageFromStorage(String path) {

        try {
            File f=new File(path, "temp.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            editPolygonView.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }

//    private void saveImagetoStorage(Bitmap bit){
//        File directory = Context.getExternalFilesDir();
//        File f = new(File)
//    }

    private String saveToExternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path
        File externalFilesDir = getExternalFilesDir(null);
        // Create savedImages
        String fileName = new SimpleDateFormat("yyyyMMddHHmm'.jpg'").format(new Date());
        File mypath=new File(externalFilesDir,fileName);

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
        return Environment.getExternalStorageDirectory()+ File.separator + fileName;
    }

    public static boolean convertToPdf(String jpgFilePath, String outputPdfPath)
    {
        try
        {
            // Check if Jpg file exists or not
            File inputFile = new File(jpgFilePath);
            if (!inputFile.exists()) throw new Exception("File '" + jpgFilePath + "' doesn't exist.");

            // Create output file if needed
            File outputFile = new File(outputPdfPath);
            if (!outputFile.exists()) outputFile.createNewFile();

//            Document document = new Document();
//            PdfWriter.getInstance(document, new FileOutputStream(outputFile));
//            document.open();
//            Image image = Image.getInstance(jpgFilePath);
//            document.add(image);
//            document.close();

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * {@link ScanbotCameraView} integrated in {@link DialogFragment} example
     */
    public static class CameraDialogFragment extends DialogFragment implements PictureCallback {
        private ScanbotCameraView cameraView;
        private ImageView resultView;
        private AutoSnappingController autoSnappingController;
        private PolygonView polygonView;
        private ContourDetectorFrameHandler contourDetectorFrameHandler;

        boolean flashEnabled = false;
        private boolean autoSnappingEnabled = true;

        /**
         * Create a new instance of CameraDialogFragment
         */
        static CameraDialogFragment newInstance() {
            return new CameraDialogFragment();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View baseView =  getActivity().getLayoutInflater().inflate(R.layout.activity_camera, container, false);

            cameraView = (ScanbotCameraView) baseView.findViewById(R.id.camera);
            cameraView.setCameraOpenCallback(new CameraOpenCallback() {
                @Override
                public void onCameraOpened() {
                    cameraView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            cameraView.continuousFocus();
                            cameraView.useFlash(flashEnabled);
                        }
                    }, 700);
                }
            });

            resultView = (ImageView) baseView.findViewById(R.id.result);

            ContourDetectorFrameHandler contourDetectorFrameHandler = ContourDetectorFrameHandler.attach(cameraView);

            PolygonView polygonView = (PolygonView) baseView.findViewById(R.id.polygonView);
            contourDetectorFrameHandler.addResultHandler(polygonView);

            AutoSnappingController.attach(cameraView, contourDetectorFrameHandler);

            cameraView.addPictureCallback(this);

            baseView.findViewById(R.id.snap).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cameraView.takePicture(false);
                }
            });

            baseView.findViewById(R.id.flash).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    flashEnabled = !flashEnabled;
                    cameraView.useFlash(flashEnabled);
                }
            });

            baseView.findViewById(R.id.autoSnappingToggle).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    autoSnappingEnabled = !autoSnappingEnabled;
                    setAutoSnapEnabled(autoSnappingEnabled);
                }
            });

            return baseView;
        }

        @Override
        public void onStart()
        {
            super.onStart();
            Dialog dialog = getDialog();
            if (dialog != null) {
                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                dialog.getWindow().setLayout(width, height);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            cameraView.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
            cameraView.onPause();
        }

        @Override
        public void onPictureTaken(final byte[] image, int imageOrientation) {
            // Here we get the full image from the camera.
            // Implement a suitable async(!) detection and image handling here.
            // This is just a demo showing detected image as downscaled preview image.

            // Decode Bitmap from bytes of original image:
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8; // use 1 for original size (if you want no downscale)!
            // in this demo we downscale the image to 1/8 for the preview.
            Bitmap originalBitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);

            // rotate original image if required:
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
    }
}