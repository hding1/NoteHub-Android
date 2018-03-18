package com.cs48.g15.notehub.Scanbot;

import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
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
import android.util.DisplayMetrics;
import android.widget.TextView;
import android.widget.Toast;

import net.doo.snap.ScanbotSDK;
import net.doo.snap.entity.Page;
//import net.doo.snap.entity.Document;
import net.doo.snap.entity.SnappingDraft;
import net.doo.snap.lib.detector.ContourDetector;
import net.doo.snap.lib.detector.DetectionResult;
import net.doo.snap.lib.detector.Line2D;
import net.doo.snap.persistence.PageFactory;
import net.doo.snap.ui.EditPolygonImageView;
import net.doo.snap.ui.MagnifierView;
import net.doo.snap.persistence.cleanup.Cleaner;
import net.doo.snap.process.DocumentProcessingResult;
import net.doo.snap.process.DocumentProcessor;
import net.doo.snap.process.draft.DocumentDraftExtractor;
import net.doo.snap.process.util.DocumentDraft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import com.cs48.g15.notehub.R;
import com.cs48.g15.notehub.UploadActivity;
import com.itextpdf.text.Document;

import com.ipaulpro.afilechooser.utils.FileUtils;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

import static android.os.Environment.getExternalStoragePublicDirectory;


public class EditPolygonImageActivity extends AppCompatActivity {

    private EditPolygonImageView editPolygonView;
    private MagnifierView magnifierView;
    private Bitmap originalBitmap;
    private View progressView;
    private ImageView resultImageView;
    private PageFactory pageFactory;
    private DocumentDraftExtractor documentDraftExtractor;
    private DocumentProcessor documentProcessor;
    private Cleaner cleaner;
    private Button cropButton, uploadButton, backButton, saveButton;
    private List<DocumentProcessingResult> results = new ArrayList<>();

    private String path;
    private String user;
    private String Path;
    private String saved;

    private Bitmap GetBitmap;
    //TextView tv;

    //private Uri uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_polygon_image);

        initializeDependencies();

        //getSupportActionBar().hide();

        editPolygonView = (EditPolygonImageView) findViewById(R.id.polygonView);

        Intent intent = getIntent();
        //path = intent.getStringExtra("Path");
        user = intent.getStringExtra("username");
        String pictureUri= intent.getStringExtra("pictureUri");
        Uri uri = Uri.parse(pictureUri);
        try {
            GetBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

//
//        GetBitmap = BitmapFactory.decodeByteArray(bis, 0, bis.length);
//

        //loadImageFromStorage(path);

//        try {
//            File f = new File(path,"temp,jpg");
//            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
//            editPolygonView.setImageBitmap(b);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }


        //BitmapDrawable p = new BitmapDrawable(getResources(),bitmap);

        //在这里要change source，真实情况应该是存在storage的某个地址
        editPolygonView.setImageBitmap(GetBitmap);
        originalBitmap = ((BitmapDrawable) editPolygonView.getDrawable()).getBitmap();

        magnifierView = (MagnifierView) findViewById(R.id.magnifier);
        // MagifierView should be set up every time when editPolygonView is set with new image
        magnifierView.setupMagnifier(editPolygonView);

        resultImageView = (ImageView) findViewById(R.id.resultImageView);
        progressView = findViewById(R.id.progressBar);
        resultImageView.setVisibility(View.GONE);

        cropButton = (Button) findViewById(R.id.cropButton);
        saveButton = (Button) findViewById(R.id.saveButton);

        uploadButton = findViewById(R.id.uploadButton);

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
                saved = saveToExternalStorage(bit);
                Toast.makeText(EditPolygonImageActivity.this, "Your pdf file has been saved to Download folder.", Toast.LENGTH_SHORT).show();


                //results = SaveBitmap2PDF(bit);
                //progressView.setVisibility(View.VISIBLE);
                //Path = getPath()；

                uploadButton.setVisibility(View.VISIBLE);

            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Path = createPdf(saved);
                    Toast.makeText(EditPolygonImageActivity.this, "Your pdf file has been saved to Download folder.", Toast.LENGTH_SHORT).show();
                } catch (DocumentException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                Intent intent = new Intent(EditPolygonImageActivity.this, UploadActivity.class);
                intent.putExtra("path",Path);
                intent.putExtra("username",user);
                startActivity(intent);
            }
        });



        new InitImageViewTask().executeOnExecutor(Executors.newSingleThreadExecutor(), originalBitmap);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //setup();
    }

//    /**
//     * Initializes variables used for convenience
//     */
//    private void setup() {
//        // Enable Android-style asset loading (highly recommended)
//        PDFBoxResourceLoader.init(getApplicationContext());
//        // Find the root of the external storage.
//        root = android.os.Environment.getExternalStorageDirectory();
//        assetManager = getAssets();
//        //tv = (TextView) findViewById(R.id.statusTextView);
//    }

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

    private void initializeDependencies() {
        ScanbotSDK scanbotSDK = new ScanbotSDK(this);
        pageFactory = scanbotSDK.pageFactory();
        documentDraftExtractor = scanbotSDK.documentDraftExtractor();
        documentProcessor = scanbotSDK.documentProcessor();
        cleaner = scanbotSDK.cleaner();
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


    private String saveToExternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path
        File externalFilesDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        // Create savedImages
        String fileName = new SimpleDateFormat("yyyyMMddHHmm'.jpg'").format(new Date());
        File mypath=new File(externalFilesDir,fileName);
        Toast.makeText(EditPolygonImageActivity.this, "Saving process will be finished in a few seconds.", Toast.LENGTH_SHORT).show();

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
        //return externalFilesDir+ File.separator + fileName;
        return fileName;
    }

    private String createPdf(String Filename) throws IOException, DocumentException {
        Document document=new Document(PageSize.A4);
        String dirpath=getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        String fileName = new SimpleDateFormat("yyyyMMddHHmm'.pdf'").format(new Date());
        PdfWriter.getInstance(document,new FileOutputStream(dirpath+"/" + fileName));

        document.open();
        String imagepath = getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
        Image img=Image.getInstance(imagepath+"/"+Filename);  // Replace logo.png with your image name with extension
        img.scalePercent(20,20);
        img.setAbsolutePosition((PageSize.A4.getWidth() - img.getScaledWidth()) / 2, (PageSize.A4.getHeight() - img.getScaledHeight()) / 2);
        document.add(img);
        document.close();

        return dirpath+ "/" + fileName;

    }



//    private String getPath(List<DocumentProcessingResult> documentProcessingResult){
//        Document document = documentProcessingResult.get(0).getDocument();
//        File documentFile = documentProcessingResult.get(0).getDocumentFile();
//        String Path;
//        Path = documentFile.getPath();
//
////        Uri uri = FileProvider.getUriForFile(EditPolygonImageActivity.this,BuildConfig.APPLICATION_ID+".fileProvider",documentFile);
//
//        return Path;
//    }

//    private List<DocumentProcessingResult> SaveBitmap2PDF(Bitmap map){
//        int screenWidth;
//        int screenHeight;
//        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//        screenWidth = displayMetrics.widthPixels;
//        screenHeight = displayMetrics.heightPixels;
//        List<DocumentProcessingResult> results = new ArrayList<>();
//
//        try {
//            Bitmap result = map;
//
//            Page page = pageFactory.buildPage(result, screenWidth, screenHeight).page;
//            SnappingDraft snappingDraft = new SnappingDraft(page);
//            DocumentDraft[] drafts = documentDraftExtractor.extract(snappingDraft);
//
//            for (DocumentDraft draft : drafts) {
//                try {
//                    results.add(documentProcessor.processDocument(draft));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            cleaner.cleanUp();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        return results;
//    }

//    private void saveImagetoStorage(Bitmap bit){
//        File directory = Context.getExternalFilesDir();
//        File f = new(File)
//    }



//    /**
//     * Creates a new PDF from scratch and saves it to a file
//     */
//    public void createPdf(Bitmap bitmap, String Filename) throws IOException {
//        try {
//            PDDocument document = new PDDocument();
//            PDPage page = new PDPage();
//            document.addPage(page);
//
//            //File f=new (Filename);
//            InputStream in = new FileInputStream(Filename);
//
//            PDImageXObject ximage = JPEGFactory.createFromStream(document, in);
//            PDPageContentStream contentStream = new PDPageContentStream(document, page);
//            contentStream.drawImage(ximage,20,20);
//
//
//            // Create a new font object selecting one of the PDF base fonts
////            PDFont font = PDType1Font.HELVETICA;
//            // Or a custom font
////		    try {
////			    PDType0Font font = PDType0Font.load(document, assetManager.open("MyFontFile.TTF"));
////		    } catch(IOException e) {
////			    e.printStackTrace();
////		    }
////
////            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
////            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
////            InputStream bs = new ByteArrayInputStream(outputStream.toByteArray());
//
//            //PDImageXObject alphaXimage = LosslessFactory.createFromImage(document, bitmap);
//
////            PDImageXObject pdImage = new PDImageXObject(document, bs,
////                    COSName.DCT_DECODE, bitmap.getWidth(), bitmap.getHeight(),
////                    8, //awtImage.getColorModel().getComponentSize(0),TODO
////                    PDDeviceRGB.INSTANCE);
//
//            //PDPageContentStream contentStream = new PDPageContentStream(document, page);
//            //contentStream.drawImage(alphaXimage, 20, 20);
//
//            contentStream.close();
//            document.save(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "test.pdf"));
//            document.close();
//        } catch(IOException e){
//            e.printStackTrace();
//        }
//    }

//    public static boolean convertToPdf(String jpgFilePath, String outputPdfPath)
//    {
//        try
//        {
//            // Check if Jpg file exists or not
//            File inputFile = new File(jpgFilePath);
//            if (!inputFile.exists()) throw new Exception("File '" + jpgFilePath + "' doesn't exist.");
//
//            // Create output file if needed
//            File outputFile = new File(outputPdfPath);
//            if (!outputFile.exists()) outputFile.createNewFile();
//
//            Document document = new Document();
//            PdfWriter.getInstance(document, new FileOutputStream(outputFile));
//            document.open();
//            Image image = Image.getInstance(jpgFilePath);
//            document.add(image);
//            document.close();
//
//            return true;
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//
//        return false;
//    }
}