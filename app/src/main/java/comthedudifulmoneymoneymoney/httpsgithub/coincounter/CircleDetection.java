package comthedudifulmoneymoneymoney.httpsgithub.coincounter;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.medianBlur;

/**
 * Created by chronos on 20-6-15.
 */
public class CircleDetection {

    private static final String TAG = "CIRCLE";

    // Fields

    // Ingelade Bitmap
    Bitmap image;

    // Array met gedetecteerde cirkels
    float[][] circles;

    // Geldwaarde per circle
    float[] circle_value;

    // Totaal waarde munten
    double totaal = 0;

    // Vaste Verhoudingen tussen diameters van munten/5ct/10ct/20ct/50ct/1e/2e
    final double[] vijfcent = {1.0,
            1.07594936709,
            0.955056179775,
            0.876288659794,
            0.913978494624,
            0.825242718447,};

    final double[] tiencent = {0.929411764706,
            1.0,
            0.887640449438,
            0.814432989691,
            0.849462365591,
            0.766990291262};

    final double[] twintigcent = {1.04705882353,
            1.12658227848,
            1.0,
            0.917525773196,
            0.956989247312,
            0.864077669903};

    final double[] vijftigcent = {1.14117647059,
            1.22784810127,
            1.08988764045,
            1.0,
            1.04301075269,
            0.941747572816};

    final double[] euro = {1.09411764706,
            1.17721518987,
            1.04494382022,
            0.958762886598,
            1.0,
            0.902912621359};

    final double[] tweeeuro = {1.21176470588,
            1.30379746835,
            1.15730337079,
            1.0618556701,
            1.10752688172,
            1.0};

    // Contructors
    CircleDetection(Bitmap image_input) {
        image = image_input;
    }

    //Methods

    // Detecteer cirkels en stop zo in circles array
    public void DetectCircles() {

        // Define stuff
        Bitmap image = this.image;
        Mat imgMat = new Mat();
        Mat imgCircles = new Mat();
        Utils.bitmapToMat(image, imgMat);

        // Convert image to greyscale
        Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_BGR2GRAY);

        // Blur image
        GaussianBlur(imgMat, imgMat, new Size(9, 9), 2, 2);

        // Detect circles
        /*HoughCircles( src_gray, circles, CV_HOUGH_GRADIENT, 1, src_gray.rows/8, 200, 100, 0, 0 );
        src_gray: Input image (grayscale)
        circles: A vector that stores sets of 3 values: x_{c}, y_{c}, r for each detected circle.
        CV_HOUGH_GRADIENT: Define the detection method. Currently this is the only one available in OpenCV
        dp = 1: The inverse ratio of resolution
        min_dist = src_gray.rows/8: Minimum distance between detected centers
        param_1 = 200: Upper threshold for the internal Canny edge detector
        param_2 = 100*: Threshold for center detection.
        min_radius = 0: Minimum radio to be detected. If unknown, put zero as default.
        max_radius = 0: Maximum radius to be detected. If unknown, put zero as default */
        Imgproc.HoughCircles(imgMat, imgCircles, Imgproc.CV_HOUGH_GRADIENT, 1, imgMat.rows() / 8, 100, 50, 0, 0);

        // Add color back to image
        Utils.bitmapToMat(image, imgMat);

        // Add circles to array
        circles = new float[imgCircles.cols()][3];
        for (int i = 0; i < imgCircles.cols(); i++) {
            imgCircles.get(0, i, circles[i]);
        }
        circle_value = new float[imgCircles.cols()];
        Log.i(TAG, "Cirkels gedetect:" + imgCircles.cols());

    }

    public void ValueCircles_by_radius() {
        CoinDetection coinDetection = new CoinDetection(circles);
        coinDetection.ValueCircles_by_radius();
        this.circle_value = coinDetection.circle_value;
    }

    public void DrawCircles() {

        // Zet Bitmap om in Matrix
        Mat imgMat = new Mat();
        Utils.bitmapToMat(image, imgMat);

        // Teken waarde per cirkel
        float[] circle = new float[3];
        for (int i = 0; i < this.circles.length; i++) {
            circle = this.circles[i];
            Point center = new Point();
            center.x = circle[0];
            center.y = circle[1];

            if (this.circle_value[i] == 0.05f)
                Core.putText(imgMat, "5 cent", center, 3, 1, new Scalar(255, 0, 0, 255), 3);
            else if (this.circle_value[i] == 0.10f)
                Core.putText(imgMat, "10 cent", center, 3, 1, new Scalar(255, 0, 0, 255), 3);
            else if (this.circle_value[i] == 0.20f)
                Core.putText(imgMat, "20 cent", center, 3, 1, new Scalar(255, 0, 0, 255), 3);
            else if (this.circle_value[i] == 0.50f)
                Core.putText(imgMat, "50 cent", center, 3, 1, new Scalar(255, 0, 0, 255), 3);
            else if (this.circle_value[i] == 1.00f)
                Core.putText(imgMat, "1 euro", center, 3, 1, new Scalar(255, 0, 0, 255), 3);
            else if (this.circle_value[i] == 2.00f)
                Core.putText(imgMat, "2 euro", center, 3, 1, new Scalar(255, 0, 0, 255), 3);

            Core.circle(imgMat, center, (int) circle[2], new Scalar(0, 0, 0, 0), 3, 8, 0);

            // Convert image back to Bitmap
            this.image = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(imgMat, this.image);
        }
    }

    public void Totaal() {
        double totaal_cur = 0;
        for (int i = 0; i < circle_value.length; i++) {
            totaal_cur += (double) circle_value[i];
        }
        this.totaal = totaal_cur;
        Log.i(TAG, "Totaal:" + this.totaal);
    }
}
