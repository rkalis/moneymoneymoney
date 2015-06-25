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
import android.util.DisplayMetrics;
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
    Matrix matrix = new Matrix();
    boolean once = false;
    Thread t = null;


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

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    /*** Image drawing ***/

    private Bitmap currentImage = null;


    @Override
    public void onImage(Bitmap argb) {

        // Draai en schaal nieuwe frame
        matrix.reset();
        matrix.postScale(((float) this.getHeight()) / argb.getWidth(), ((float) this.getWidth()) / argb.getHeight());


        //argb = Bitmap.createBitmap(argb, 0, 0, argb.getWidth(), argb.getHeight(), matrix, true); */

        // Laad nieuwe frame
        CD_done.LoadImage(argb);

        // Alleen eerste frame (bij opstarten camera)
        if (t == null) {
            Log.i("Thread", "Threading begonnen");
            //











            // Doe eerste berekening in main Thread
            CD_done.DetectCircles();
            CD_done.ColorDetetion();
            CD_done.ValueCircles_by_radius();
            CD_done.Totaal();

            // Start nieuwe Thread
            CD_cur = new CircleDetection(argb);
            t = new Thread(CD_cur);
            t.start();
        }

        // Als de Thread klaar is met rekenen
        if (!this.t.isAlive()) {
            // Einde Thread afhandelen
            CD_done = CD_cur;
            CD_done.LoadImage(argb);

            // Nieuwe Thread beginnen
            CD_cur = new CircleDetection(argb);
            t = new Thread(CD_cur);
            t.start();
        }

        // Geef frame door
        this.currentImage = CD_done.image;
        this.invalidate();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /* If there is an image to be drawn: */
        if (this.currentImage != null) {

            // Teken meest recente cirkels + totaal op frame
            CD_done.DrawCircles();
            MainActivity.text.setText("Totaal: " + String.format("%.2f", CD_done.totaal));

            matrix.postRotate(90);
            matrix.postTranslate(canvas.getWidth(), dpToPx(30));
            canvas.setMatrix(matrix);
            canvas.drawBitmap(CD_done.image, 0, 0, null);
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