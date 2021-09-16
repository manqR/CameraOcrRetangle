package id.indomobil.camera.focus;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    Context ctx;
    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    ImageView btnCapt;
    ProgressBar spinner;
    private int take_image = 0;
    private Vision vision;
    private static final int PERMISSION_REQUEST_CODE = 200;
    TextView TotalizerRead;

    private Mat mat;
    private boolean showPreviews;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkPermission()) {
            requestPermission();
        }

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.camera_surface_view);
        cameraBridgeViewBase.setVisibility(View.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        ctx = getApplicationContext();
        btnCapt = (ImageView)findViewById(R.id.capture);
        TotalizerRead = (TextView) findViewById(R.id.TotalizerRead);
        spinner = (ProgressBar) findViewById(R.id.spinner);
        spinner.setVisibility(View.GONE);

        //API VISION
        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null);

        visionBuilder.setVisionRequestInitializer(
                new VisionRequestInitializer("AIzaSyBcVa1effgH1NBEGEh8bwpNfh9ZS-mN7-Y"));

        vision = visionBuilder.build();

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                switch (status){
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        cameraBridgeViewBase.setCvCameraViewListener(MainActivity.this);
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };

        btnCapt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(take_image == 0){
                    take_image = 1;
                    spinner.setVisibility(View.VISIBLE);
                    btnCapt.setVisibility(View.GONE);
                }else{
                    take_image=0;
                }

            }
        });

    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat mrgba = inputFrame.rgba();

        int w = mrgba.width();
        int h = mrgba.height();

        int p1 = w / 4;
        int p2 = h  / 4;
        int p3 = (w / 2) + p2;
        int p4 = h - p2;

        Imgproc.rectangle(mrgba, new Point(p1, p2), new Point(
                p3, p4 ), new Scalar( 0, 255, 0 ), 3
        );


        take_image = take_picture_function_rgb(p1,p2,p3,p4, take_image, mrgba);

        return mrgba;
    }

    private int take_picture_function_rgb(int p1, int p2, int p3, int p4, int take_image, Mat mrgba) {
        if(take_image == 1){
//            Mat save_mat = new Mat();
//            Core.flip(mrgba.t(),save_mat,1);
//            Imgproc.cvtColor(save_mat, save_mat, Imgproc.COLOR_RGBA2BGRA);
//            Bitmap bm = Bitmap.createBitmap(320, 128,Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(mrgba, bm);

            Bitmap bmp = null;
            Bitmap bmpCrop = null;
//            Mat tmp = new ;
            try {
                //Imgproc.cvtColor(seedsImage, tmp, Imgproc.COLOR_RGB2BGRA);
//                Imgproc.cvtColor(mrgba, tmp, Imgproc.COLOR_GRAY2RGBA);
                Rect rectCrop = new Rect(p3,p2 ,(p2+p3)-35,p2 );
                Mat image_output= mrgba.submat(rectCrop);
//                Imgproc.cvtColor(mrgba, image_output,Imgproc.COLOR_GRAY2RGBA);
                bmpCrop = Bitmap.createBitmap(image_output.cols(), image_output.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(image_output, bmpCrop);

                bmp = Bitmap.createBitmap(mrgba.cols(), mrgba.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mrgba, bmp);

                Bitmap bitmapResize = Bitmap.createScaledBitmap(bmpCrop, 160, 120, false);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmapResize.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                TextDetection(byteArray);

//                Rect rectCrop = new Rect(new Point(w * 2 / 3, h * 1 / 3), new Point(
//                        w * 1 / 4, h * 2 /  3 ) );
//                Mat image_output= mrgba.submat(rectCrop);
//                bmpCrop = Bitmap.createBitmap(image_output.cols(), image_output.rows(), Bitmap.Config.ARGB_8888);
//                Utils.matToBitmap(image_output, bmpCrop);

//                Mat image_original;
//                Point p1 = null,p2,p3,p4 = null;
//                Rect rectCrop = new Rect(p1.x, p1.y , (p4.x-p1.x+1), (p4.y-p1.y+1));
//                Mat image_output= mrgba.submat(rectCrop);

                take_image = 0;
            }
            catch (CvException e){
                Log.d("Exception",e.getMessage());}
        }

        return take_image;
    }


    private void TextDetection(final byte[] img) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    Image inputImage = new Image();
                    inputImage.encodeContent(img);

                    Feature desiredFeature = new Feature();
                    desiredFeature.setType("TEXT_DETECTION");

                    AnnotateImageRequest request = new AnnotateImageRequest();
                    request.setImage(inputImage);
                    request.setFeatures(Arrays.asList(desiredFeature));

                    BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
                    batchRequest.setRequests(Arrays.asList(request));

                    BatchAnnotateImagesResponse batchResponse =
                            vision.images().annotate(batchRequest).execute();

                    final TextAnnotation text = batchResponse.getResponses()
                            .get(0).getFullTextAnnotation();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            TotalizerRead.setText(text.getText());
                            spinner.setVisibility(View.GONE);
                            btnCapt.setVisibility(View.VISIBLE);
                        }
                    });

                } catch(Exception e) {
                    Log.d("ERROR", e.getMessage());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Toast.makeText(this, "Theres some problem", Toast.LENGTH_SHORT).show();
        }else{
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
    }




    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();

                    // main logic
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermission();
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


}