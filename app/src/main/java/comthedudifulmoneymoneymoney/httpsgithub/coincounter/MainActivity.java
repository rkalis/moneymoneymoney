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
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.TextView;

import static org.opencv.imgproc.Imgproc.medianBlur;

import comthedudifulmoneymoneymoney.httpsgithub.coincounter.CameraImageSource;


public class MainActivity extends ActionBarActivity{

    private static final String TAG = "MainActivity";
    private static final int CAMERA_REQUEST = 1888;
    private static int RESULT_LOAD_IMAGE = 1;
    private ImageDisplayView view;
    private Uri imageUri;
    private Bitmap bmp;
    public static TextView text;
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

    private CameraImageSource cis;
    private boolean resumeCamera;

    private Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.cis = new CameraImageSource(this);

        this.text = (TextView) this.findViewById(R.id.testText);
        this.view = (ImageDisplayView) this.findViewById(R.id.image_display_view);

        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
        } else {
            this.loadButtons();
        }

        this.resumeCamera = false;

        if (savedInstanceState != null) {
            image = savedInstanceState.getParcelable("bitmap");
            view.onImage(image);
        }


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
//                File storageDir;
//                try {
//                    File dir = getExternalFilesDir(null);
//                    storageDir = File.createTempFile("camera_img", ".jpg", dir);
//                } catch (IOException e) {
//                    return;
//                }

                /* Set camera as active source: */
                ImageDisplayView idv = (ImageDisplayView)findViewById(R.id.image_display_view);
                if (idv.getImageSource() != MainActivity.this.cis) {
                    idv.setImageSource(MainActivity.this.cis);
                }
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
            view.onImage(image);
        } else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            cis.setOnImageListener(view);
        }

    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("bitmap", image);
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

    @Override
    protected void onPause() {
        super.onPause();
        if(cis.hasCamera) {
            this.resumeCamera = true;
            cis.releaseCamera();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(this.resumeCamera) {
            cis.acquireCamera();
        }
    }

}

