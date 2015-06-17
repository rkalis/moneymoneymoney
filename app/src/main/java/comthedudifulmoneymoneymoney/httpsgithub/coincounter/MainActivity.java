package comthedudifulmoneymoneymoney.httpsgithub.coincounter;

import java.io.File;
import java.io.IOException;

import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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


public class MainActivity extends ActionBarActivity {

    private static final int CAMERA_REQUEST = 1888;
    private static int RESULT_LOAD_IMAGE = 1;
    private ImageView view;
    private Uri imageUri;

    private Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        this.view = (ImageView) this.findViewById(R.id.image_view);

        if (savedInstanceState != null) {
            image = savedInstanceState.getParcelable("bitmap");
            view.setImageBitmap(image);
        }

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
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            int picw = BitmapFactory.decodeFile(picturePath).getWidth();
            int pich = BitmapFactory.decodeFile(picturePath).getHeight();

            int[] pix = new int[picw * pich];
            BitmapFactory.decodeFile(picturePath).getPixels(pix, 0, picw, 0, 0, picw, pich);

            view.setImageBitmap(Bitmap.createBitmap(pix, picw, pich, Bitmap.Config.ARGB_8888));
        }

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            try {
                image = MediaStore.Images.Media.getBitmap(
                        this.getContentResolver(),
                        MainActivity.this.imageUri);

                int picw = image.getWidth();
                int pich = image.getHeight();
                int[] pix = new int[picw * pich];
                image.getPixels(pix, 0, picw, 0, 0, picw, pich);

                // Circle Detection
                int[] process = pix.clone();
                circleHough test = new circleHough();
                test.init(process, picw, pich, 200);
                process = test.process();

            // Draw both the image and the circles
            for(int i = 0; i < process.length; i++) {
                pix[i] = (0xFF << 24) | (((Color.red(process[i])) | (Color.red(pix[i]))) << 16) |
                        (((Color.green(process[i])) | (Color.green(pix[i]))) << 8) |
                        (((Color.blue(process[i])) | (Color.blue(pix[i]))));
            }
                this.view.setImageBitmap(Bitmap.createBitmap(pix, picw, pich, Bitmap.Config.ARGB_8888));
            } catch(IOException e) {
            }
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
}
