package comthedudifulmoneymoneymoney.httpsgithub.coincounter;

import java.io.File;
import java.io.IOException;

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
    private ImageView view;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.view = (ImageView) this.findViewById(R.id.image_view);

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
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(
                        this.getContentResolver(),
                        MainActivity.this.imageUri);
                this.view.setImageBitmap(image);
            } catch(IOException e) {
            }
        }
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
