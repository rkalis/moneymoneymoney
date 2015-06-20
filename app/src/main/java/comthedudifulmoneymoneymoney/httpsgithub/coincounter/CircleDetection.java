package comthedudifulmoneymoneymoney.httpsgithub.coincounter;

import android.graphics.Bitmap;

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

    // Fields
    Bitmap image;
    int[] Coins = new int[100];

    // Contructors
    CircleDetection(Bitmap image_input) {
        image = image_input;
    }

    //Methods
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
        //medianBlur(imgMat, imgMat, 5);

        // Detect circles

        /*HoughCircles( src_gray, circles, CV_HOUGH_GRADIENT, 1, src_gray.rows/8, 200, 100, 0, 0 );
        with the arguments:

        src_gray: Input image (grayscale)
        circles: A vector that stores sets of 3 values: x_{c}, y_{c}, r for each detected circle.
        CV_HOUGH_GRADIENT: Define the detection method. Currently this is the only one available in OpenCV
        dp = 1: The inverse ratio of resolution
        min_dist = src_gray.rows/8: Minimum distance between detected centers
        param_1 = 200: Upper threshold for the internal Canny edge detector
        param_2 = 100*: Threshold for center detection.
        min_radius = 0: Minimum radio to be detected. If unknown, put zero as default.
        max_radius = 0: Maximum radius to be detected. If unknown, put zero as default */
        //Imgproc.HoughCircles(imgMat, imgCircles, Imgproc.CV_HOUGH_GRADIENT, 1, imgMat.rows() / 8, 100, 50, 100, 0);
        Imgproc.HoughCircles(imgMat, imgCircles, Imgproc.CV_HOUGH_GRADIENT, 1, imgMat.rows() / 8, 100, 50, 0, 0);

        // Add color back to image
        Utils.bitmapToMat(image, imgMat);

        float[] circle = new float[3];
        for (int i = 0; i < imgCircles.cols(); i++) {
            imgCircles.get(0, i, circle);
            Point center = new Point();
            center.x = circle[0];
            center.y = circle[1];
            //Core.circle(imgMat, center, 3,new Scalar(255,255,255), -1, 8, 0 );
            if ((int)circle[2] > 180)
                Core.putText(imgMat, "2 Euro", center, 3/* CV_FONT_HERSHEY_COMPLEX */, 1, new Scalar(255, 0, 0, 255), 3);
            else if ((int)circle[2] > 170)
                Core.putText(imgMat, "1 Euro", center, 3/* CV_FONT_HERSHEY_COMPLEX */, 1, new Scalar(255, 0, 0, 255), 3);
            else if ((int)circle[2] > 160)
                Core.putText(imgMat, "20 cent", center, 3/* CV_FONT_HERSHEY_COMPLEX */, 1, new Scalar(255, 0, 0, 255), 3);
            else if ((int)circle[2] > 157)
                Core.putText(imgMat, "50 cent", center, 3/* CV_FONT_HERSHEY_COMPLEX */, 1, new Scalar(255, 0, 0, 255), 3);
            else if ((int)circle[2] > 140)
                Core.putText(imgMat, "5 cent", center, 3/* CV_FONT_HERSHEY_COMPLEX */, 1, new Scalar(255, 0, 0, 255), 3);
            else if ((int)circle[2] > 130)
                Core.putText(imgMat, "10 cent", center, 3/* CV_FONT_HERSHEY_COMPLEX */, 1, new Scalar(255, 0, 0, 255), 3);
            Core.circle(imgMat, center, (int) circle[2], new Scalar(0, 0, 0, 0), 3, 8, 0);
            Coins[i] = (int) circle[2];

            // Convert image back to Bitmap
            this.image = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(imgMat, this.image);
        }
    }
}
