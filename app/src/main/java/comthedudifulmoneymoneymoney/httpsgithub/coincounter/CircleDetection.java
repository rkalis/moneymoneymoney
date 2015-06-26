package comthedudifulmoneymoneymoney.httpsgithub.coincounter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.GaussianBlur;

/**
 * Created by chronos on 20-6-15.
 */
public class CircleDetection implements Runnable {

    private static final String TAG = "CD";

    // Fields

    private int[][] sd_array;

    // Ingelade Bitmap
    Bitmap image;

    // Array met gedetecteerde cirkels
    float[][] circles;

    // Geldwaarde per circle
    float[] circle_value;

    // Totaal waarde munten
    float totaal = 0;

    // Vaste Verhoudingen tussen diameters van munt/5ct/10ct/20ct/50ct/1e/2e
    final float[] vijfcent = {//1.0,
            1.07594936709f,
            0.955056179775f,
            0.876288659794f,
            0.913978494624f,
            0.825242718447f,};

    final float[] tiencent = {0.929411764706f,
            //1.0,
            0.887640449438f,
            0.814432989691f,
            0.849462365591f,
            0.766990291262f};

    final float[] twintigcent = {1.04705882353f,
            1.12658227848f,
            //1.0,
            0.917525773196f,
            0.956989247312f,
            0.864077669903f};

    final float[] vijftigcent = {1.14117647059f,
            1.22784810127f,
            1.08988764045f,
            //1.0,
            1.04301075269f,
            0.941747572816f};

    final float[] euro = {1.09411764706f,
            1.17721518987f,
            1.04494382022f,
            0.958762886598f,
            //1.0,
            0.902912621359f};

    final float[] tweeeuro = {1.21176470588f,
            1.30379746835f,
            1.15730337079f,
            1.0618556701f,
            1.10752688172f/*,
            1.0*/};

    final float[] inverse_vijfcent = {
            //1.0, vijfcent (ColorDetection)
            0.929411764706f,
            1.04705882353f,
            1.14117647059f,
            //1.09411764706f,euro (ColorDetection)
            1.21176470588f};

    final float[] inverse_euro = {//0.913978494624f,
            0.849462365591f,
            0.956989247312f,
            1.04301075269f,
            //1.0,
            1.10752688172f};

    // Contructors
    CircleDetection(){}

    CircleDetection(Bitmap image_input) {
        this.image = image_input;
    }

    //Methods

    public void LoadImage(Bitmap image_input){
        this.image = image_input;
    }

    // Detecteer cirkels en stop zo in circles array
    public void DetectCircles() {

        // Define stuff
        Bitmap image = this.image;
        Mat imgMat = new Mat();
        Mat imgCircles = new Mat();
        Utils.bitmapToMat(image, imgMat);

        //Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_BGR2HSV);
        //Core.inRange(imgMat, new Scalar(0, 90, 90), new Scalar(10, 110, 110), imgMat);

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
        //Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_BGR2HSV,3);

        // Add circles to array
        circles = new float[imgCircles.cols()][3];
        for (int i = 0; i < imgCircles.cols(); i++) {
            imgCircles.get(0, i, circles[i]);
        }
        circle_value = new float[imgCircles.cols()];
        Log.i(TAG, "Cirkels gedetect:" + imgCircles.cols());

    }

    // Bepaal waarden van circles door middel van diameter analyse
    public void ValueCircles_by_radius() {

        Log.i(TAG, "DIAMETERANALYSE BEGONNEN");

        // Loop door circles die een waarde moeten krijgen
        for (int i = 0; i < circles.length; i++) {

            // Als de waarde van de cirkel nog niet bepaald is (géén 1 euro of 5 cent, zie ColorDet)
            if (this.circle_value[i] == 0.10f) {

                // Bepaal waarde cirkel op basis van "voting" dmv vergelijken diameters
                int[] votes = new int[6];
                float lowest_diff, current_diff;
                int currentvote;
                int factor = 1;

                lowest_diff = 10.0f;
                currentvote = 0;

                // Loop door overige cirkels, en "Vraag" aan iedere cirkel welke waarde je het meest
                // waarschijnlijk bent volgens de deling met hun diameter
                for (int j = 0; j < this.circles.length; j++) {

                    lowest_diff = 10.0f;
                    currentvote = 0;

                    factor = 1;

                    // Niet vergelijken met zichzelf
                    if (j != i) {

                        // Als de andere munt (de deler) 5 cent is
                        if(circle_value[j] == 0.05f) {

                            int index = 0;
                            factor = 2;

                            for (int a = 0; a < this.inverse_vijfcent.length; a++) {

                                // Als de diameter deling significant lijkt op die van vaste verhouding
                                current_diff = Math.abs(this.inverse_vijfcent[a] - (this.circles[i][2]
                                        / this.circles[j][2]));

                                if (current_diff < lowest_diff) {
                                    index = a;
                                    lowest_diff = current_diff;
                                }
                            }

                            switch (index) {
                                case 0:
                                    currentvote = 1;
                                    break;
                                case 1:
                                    currentvote = 2;
                                    break;
                                case 2:
                                    currentvote = 3;
                                    break;
                                case 3:
                                    currentvote = 5;
                                    break;
                            }
                        }
                        // Als de andere munt (de deler) 1 euro is
                        else if(circle_value[j] == 1.00f) {

                            int index = 0;
                            factor = 2;

                            for (int a = 0; a < this.inverse_euro.length; a++) {

                                // Als de diameter deling significant lijkt op die van vaste verhouding
                                current_diff = Math.abs(this.inverse_euro[a] - (this.circles[i][2]
                                        / this.circles[j][2]));

                                if (current_diff < lowest_diff) {
                                    index = a;
                                    lowest_diff = current_diff;
                                }
                            }

                            switch (index) {
                                case 0:
                                    currentvote = 1;
                                    break;
                                case 1:
                                    currentvote = 2;
                                    break;
                                case 2:
                                    currentvote = 3;
                                    break;
                                case 3:
                                    currentvote = 5;
                                    break;
                            }
                        }
                        // Als de deler 10ct, 20ct, 50ct of 2 euro is.
                        else {

                            factor = 1;

                            for (int a = 0; a < this.tiencent.length; a++) {

                                // Als de diameter deling significant lijkt op die van vaste verhouding
                                current_diff = Math.abs(this.tiencent[a] - (this.circles[i][2]
                                        / this.circles[j][2]));

                                if (current_diff < lowest_diff) {
                                    lowest_diff = current_diff;
                                    currentvote = 1;
                                }
                            }

                            for (int a = 0; a < this.twintigcent.length; a++) {

                                // Als de diameter deling significant lijkt op die van vaste verhouding
                                current_diff = Math.abs(this.twintigcent[a] - (this.circles[i][2]
                                        / this.circles[j][2]));

                                if (current_diff < lowest_diff) {
                                    lowest_diff = current_diff;
                                    currentvote = 2;
                                }
                            }

                            for (int a = 0; a < this.vijftigcent.length; a++) {

                                // Als de diameter deling significant lijkt op die van vaste verhouding
                                current_diff = Math.abs(this.vijftigcent[a] - (this.circles[i][2]
                                        / this.circles[j][2]));

                                if (current_diff < lowest_diff) {
                                    lowest_diff = current_diff;
                                    currentvote = 3;
                                }
                            }

                            for (int a = 0; a < this.tweeeuro.length; a++) {

                                // Als de diameter deling significant lijkt op die van vaste verhouding
                                current_diff = Math.abs(this.tweeeuro[a] - (this.circles[i][2]
                                        / this.circles[j][2]));

                                if (current_diff < lowest_diff) {
                                    lowest_diff = current_diff;
                                    currentvote = 5;
                                }
                            }

                            // Filter deling met delzelfde munt
                            if(Math.abs(1.0f - (this.circles[i][2]
                                    / this.circles[j][2])) < lowest_diff) {
                                lowest_diff = 10.0f;
                            }
                        }
                    }
                }

                if (lowest_diff < 9.0) {
                    votes[currentvote] += 1*factor;
                }

                // Bepaal welke munt het is op basis van de votes
                int munt = 0;
                int max = 0;
                for (int z = 0; z < votes.length; z++) {
                    if (votes[z] > max) {
                        max = votes[z];
                        munt = z;
                    }
                }


                // Geef waarde aan de munt als er tenminste 1 valide stem was
                if (max > 0) {
                    switch (munt) {
                        case 0:
                            this.circle_value[i] = 0.05f;
                            break;
                        case 1:
                            this.circle_value[i] = 0.10f;
                            break;
                        case 2:
                            this.circle_value[i] = 0.20f;
                            break;
                        case 3:
                            this.circle_value[i] = 0.50f;
                            break;
                        case 4:
                            this.circle_value[i] = 1.00f;
                            break;
                        case 5:
                            this.circle_value[i] = 2.00f;
                            break;
                    }
                }
            }
        }
    }


    public void ColorDetetion() {
        int picw = image.getWidth();
        int pich = image.getHeight();

        /* create argb array from bitmap */
        int[] pix = new int[picw * pich];
        image.getPixels(pix, 0, picw, 0, 0, picw, pich);

        int aantal;
        float r;
        float g;
        float b;

        /* array that will hold the red, green, blue and hsv values */
        sd_array = new int[circles.length][6];

        /* loop through all the cirkles */
        for (int i = 0; i < circles.length; i++) {
            aantal = 0;
            r = 0;
            g = 0;
            b = 0;

            for (int x = -(int)(circles[i][2]/2); x< (int)(circles[i][2]/2); x++) {
                for (int y = -(int)(circles[i][2]/2); y < (int)(circles[i][2]/2)  ; y++){
                    r += (0xFF & (pix[calculateInt((double) (x + circles[i][0]),
                            (double) (circles[i][1]))] >> 16));
                    g += (0xFF & (pix[calculateInt((double) (x + circles[i][0]),
                            (double) (circles[i][1]))] >> 8));
                    b += (0xFF & (pix[calculateInt((double) (x + circles[i][0]),
                            (double) (circles[i][1]))] >> 0));
                    aantal++;
                }
            }

            sd_array[i][0] = (int)(r/aantal);
            sd_array[i][1] = (int)(g/aantal);
            sd_array[i][2] = (int)(b/aantal);

            float[] hsv = new float[3];
            Color.RGBToHSV(sd_array[i][0], sd_array[i][1], sd_array[i][2], hsv);

            sd_array[i][3] = (int)hsv[0];
            sd_array[i][4] = (int)(hsv[1]*100);
            sd_array[i][5] = (int)(hsv[2]*100);

            if (sd_array[i][3] < 25 && sd_array[i][3] > 0) this.circle_value[i] = 0.05f;
            else if (sd_array[i][3] > 65) this.circle_value[i] = 1.00f;
            else this.circle_value[i] = 0.10f;

        }
    }

    /* calculate the mean */
    public double calcMean(int[] m, int aantal) {
        double sum = 0;
        /* loop trough array and sum all values */
        for (int i = 0; i < aantal; i++) {
            sum += m[i];
        }
        return sum / m.length;
    }

    /* calculat the standard deviation */
    public double calcSd (int[] m, int aantal) {
        double sum = 0;
        double mean = calcMean(m, aantal);

        /* take the power of 2 of each value minus the mean and sum these */
        for (int i = 0; i < m.length; i++)
            sum += (m[i] - mean) * (m[i] - mean);
        /* calculate root of sum / lenght */
        return Math.sqrt(sum / m.length);
    }

    /* calculate the array index value from the x and y coordinate */
    public int calculateInt (double x, double y){
        int index = 0;
        index = (int)((int)(y) * image.getWidth() + (int)(x));
        return index;
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
                Core.putText(imgMat, "5 cent" + " " + Integer.toString(sd_array[i][3]) + " " + Integer.toString(sd_array[i][4])+ " " + Integer.toString(sd_array[i][5]), center, 3, 1, new Scalar(255, 0, 0, 255), 3);
            else if (this.circle_value[i] == 0.10f)
                Core.putText(imgMat, "10 cent" + " " + Float.toString(sd_array[i][3]) + " " + Float.toString(sd_array[i][4])+ " " + Float.toString(sd_array[i][5]), center, 3, 1, new Scalar(255, 0, 0, 255), 3);
            else if (this.circle_value[i] == 0.20f)
                Core.putText(imgMat, "20 cent" + " " + Float.toString(sd_array[i][3]) + " " + Float.toString(sd_array[i][4])+ " " + Float.toString(sd_array[i][5]), center, 3, 1, new Scalar(255, 0, 0, 255), 3);
            else if (this.circle_value[i] == 0.50f)
                Core.putText(imgMat, "50 cent" + " " + Float.toString(sd_array[i][3]) + " " + Float.toString(sd_array[i][4])+ " " + Float.toString(sd_array[i][5]), center, 3, 1, new Scalar(255, 0, 0, 255), 3);
            else if (this.circle_value[i] == 1.00f)
                Core.putText(imgMat, "1 euro" + " " + Float.toString(sd_array[i][3]) + " " + Float.toString(sd_array[i][4])+ " " + Float.toString(sd_array[i][5]), center, 3, 1, new Scalar(255, 0, 0, 255), 3);
            else if (this.circle_value[i] == 2.00f)
                Core.putText(imgMat, "2 euro" + " " + Float.toString(sd_array[i][3]) + " " + Float.toString(sd_array[i][4])+ " " + Float.toString(sd_array[i][5]), center, 3, 1, new Scalar(255, 0, 0, 255), 3);

            Core.circle(imgMat, center, (int) circle[2], new Scalar(0, 0, 0, 0), 3, 8, 0);

            // Convert image back to Bitmap
            this.image = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(imgMat, this.image);
        }
    }

    public void Totaal() {
        float totaal_cur = 0.0f;
        for (int i = 0; i < circle_value.length; i++) {
            totaal_cur += circle_value[i];
        }
        this.totaal = totaal_cur;
        Log.i(TAG, "Totaal:" + this.totaal);
    }

    @Override
    public void run() {
            this.DetectCircles();
            this.ColorDetetion();
            this.ValueCircles_by_radius();
            this.Totaal();
    }
}
