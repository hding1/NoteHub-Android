package com.cs48.g15.notehub;

import android.content.ContextWrapper;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.view.WindowCompat;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.content.Intent;

import net.doo.snap.lib.detector.ContourDetector;
import net.doo.snap.lib.detector.DetectionResult;
import net.doo.snap.lib.detector.Line2D;
import net.doo.snap.ui.EditPolygonImageView;
import net.doo.snap.ui.MagnifierView;

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
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

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

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            document.open();
            Image image = Image.getInstance(jpgFilePath);
            document.add(image);
            document.close();

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }
}