package comthedudifulmoneymoneymoney.httpsgithub.coincounter;

import java.io.File;
import java.io.IOException;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.Utils;

import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Environment;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.util.Log;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";
    private static final int CAMERA_REQUEST = 1888;
    private static int RESULT_LOAD_IMAGE = 1;
    private ImageView view;
    private Uri imageUri;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                    MainActivity.this.loadButtons();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
        } else {
            this.loadButtons();
        }
        this.view = (ImageView) this.findViewById(R.id.image_view);
    }

    private void loadButtons() {
        Button buttonLoadImage = (Button) findViewById(R.id.image_button);
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        Button cameraButton = (Button) this.findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File storageDir;
                try {
                    File dir = getExternalFilesDir(null);
                    storageDir = File.createTempFile("camera_img", ".jpg", dir);
                } catch (IOException e) {
                    return;
                }

                MainActivity.this.imageUri = Uri.fromFile(storageDir);
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MainActivity.this.imageUri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap image;

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            image = BitmapFactory.decodeFile(picturePath);
            this.viewBitmap(image);
        } else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            try {
                image = MediaStore.Images.Media.getBitmap(
                        this.getContentResolver(),
                        MainActivity.this.imageUri);
                this.viewBitmap(image);
            } catch(IOException e) {
            }
        }

    }

    // view this bitmap in the view
    private void viewBitmap(Bitmap image) {
        int picw = image.getWidth();
        int pich = image.getHeight();
        int[] pix = new int[picw * pich];

        image.getPixels(pix, 0, picw, 0, 0, picw, pich);
        image = Bitmap.createBitmap(pix, picw, pich, Bitmap.Config.ARGB_8888);

        Mat imgMat = new Mat();
        Mat imgCircles = new Mat();
        Utils.bitmapToMat(image, imgMat);
        // convert image to greyscale
        Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_BGR2GRAY);
        // blur image
        //Imgproc.GaussianBlur(imgMat, imgMat, new Size(9, 9), 2, 2);
        // detect circles
        Imgproc.HoughCircles(imgMat, imgCircles, Imgproc.CV_HOUGH_GRADIENT, 1, imgMat.rows()/16, 200, 50, 0, 0);

        Log.d(TAG, "circles detected: " + imgCircles.cols());

        float[] circle = new float[3];
        for (int i = 0; i < imgCircles.cols(); i++) {
            imgCircles.get(0, i, circle);
            Point center = new Point();
            center.x = circle[0];
            center.y = circle[1];
            Core.circle(imgMat, center, (int) circle[2], new Scalar(255,255,0,0), 10);
        }
        Bitmap bmp = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imgMat, bmp);
        this.view.setImageBitmap(bmp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
