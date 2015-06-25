/*
 * Framework code written for the Multimedia course taught in the first year
 * of the UvA Informatica bachelor.
 *
 * Nardi Lam, 2015 (based on code by I.M.J. Kamps, S.J.R. van Schaik, R. de Vries, 2013)
 */

package comthedudifulmoneymoneymoney.httpsgithub.coincounter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.graphics.Color;
import android.graphics.Bitmap;
import java.util.Arrays;

/*
 * This is a View that displays incoming images.
 */
public class ImageDisplayView extends View implements ImageListener {

    CircleDetection CD_cur = new CircleDetection();
    CircleDetection CD_done = new CircleDetection();
    boolean processing = false;
    Thread t = null;
    Bitmap temp;


    /*** Constructors ***/

    public ImageDisplayView(Context context) {
        super(context);
    }

    public ImageDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageDisplayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /*** Image drawing ***/

    private Bitmap currentImage = null;


    @Override
    public void onImage(Bitmap argb) {

        // Draai Bitmap
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        this.currentImage = Bitmap.createBitmap(argb, 0, 0, argb.getWidth(), argb.getHeight(), matrix, true);

        CD_done.LoadImage(this.currentImage);

        // Eerste Bitmap (bij opstarten camera)
        if(t == null) {
            // Doe eerste berekning in main Thread
            CD_done.DetectCircles();
            CD_done.ColorDetetion();
            CD_done.ValueCircles_by_radius();
            CD_done.Totaal();

            // Start nieuwe Thread
            CD_cur = new CircleDetection(this.currentImage);
            t = new Thread(CD_cur);
            t.start();
        }

        // Als de Thread klaar is met rekenen
        if (!this.t.isAlive()) {

            // Einde Thread afhandelen
            CD_done = CD_cur;
            CD_done.LoadImage(this.currentImage);

            // Nieuwe Thread beginnen
            CD_cur = new CircleDetection(this.currentImage);
            t = new Thread(CD_cur);
            t.start();
        }

        // Teken meest recente cirkels + totaal
        CD_done.DrawCircles();
        MainActivity.text.setText("Totaal: " + String.format("%.2f", CD_done.totaal));

        // Schaal Bitmap voor op het scherm
        this.currentImage = Bitmap.createScaledBitmap(CD_done.image, this.getWidth(), this.getHeight(), true);

        this.invalidate();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /* If there is an image to be drawn: */
        if (this.currentImage != null) {
            canvas.drawBitmap(this.currentImage, 0, 0, null);
        }
    }

    /*** Source selection ***/
    private ImageSource source = null;

    public void setImageSource(ImageSource source) {
        if (this.source != null) {
            this.source.setOnImageListener(null);
        }
        source.setOnImageListener(this);
        this.source = source;
    }

    public ImageSource getImageSource() {
        return this.source;
    }

}