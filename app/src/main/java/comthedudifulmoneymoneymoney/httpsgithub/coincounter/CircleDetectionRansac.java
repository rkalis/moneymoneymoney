package comthedudifulmoneymoneymoney.httpsgithub.coincounter;


import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Point;
import java.util.ArrayList;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Scalar;

/**
 * Created by bas on 25-6-15.
 */
public class CircleDetectionRansac {

    Bitmap image;

    private class Circle {
        public Point center;
        public double radius;
    }

    CircleDetectionRansac(Bitmap image) {
        this.image = image;
    }

    Circle getCircle(Point p1, Point p2, Point p3)
    {
        double x1 = p1.x;
        double x2 = p2.x;
        double x3 = p3.x;

        double y1 = p1.y;
        double y2 = p2.y;
        double y3 = p3.y;

        Circle circle = new Circle();

        // PLEASE CHECK FOR TYPOS IN THE FORMULA :)
        circle.center.x = (x1*x1+y1*y1)*(y2-y3) + (x2*x2+y2*y2)*(y3-y1) + (x3*x3+y3*y3)*(y1-y2);
        circle.center.x /= ( 2*(x1*(y2-y3) - y1*(x2-x3) + x2*y3 - x3*y2) );

        circle.center.y = (x1*x1 + y1*y1)*(x3-x2) + (x2*x2+y2*y2)*(x1-x3) + (x3*x3 + y3*y3)*(x2-x1);
        circle.center.y /= ( 2*(x1*(y2-y3) - y1*(x2-x3) + x2 * y3 - x3 * y2));

        circle.radius = Math.sqrt((circle.center.x - x1) * (circle.center.x - x1) +
                (circle.center.y-y1)*(circle.center.y-y1));

        return circle;
    }



    ArrayList<Point> getPointPositions(Mat binaryImage)
    {
        ArrayList<Point> pointPositions = new ArrayList<>();

        for(int y=0; y<binaryImage.rows(); ++y)
        {
            //unsigned char* rowPtr = binaryImage.ptr<unsigned char>(y);
            for(int x=0; x<binaryImage.cols(); ++x)
            {
                //if(rowPtr[x] > 0) pointPositions.push_back(cv::Point2i(x,y));
                if(binaryImage.get(y,x)[0] > 0.0) pointPositions.add(new Point(x,y));
            }
        }

        return pointPositions;
    }


    float verifyCircle(Mat dt, Point center, double radius, ArrayList<Point> inlierSet)
    {
        int counter = 0;
        int inlier = 0;
        double minInlierDist = 2.0f;
        double maxInlierDistMax = 100.0f;
        double maxInlierDist = radius/25.0f;
        if(maxInlierDist<minInlierDist) maxInlierDist = minInlierDist;
        if(maxInlierDist>maxInlierDistMax) maxInlierDist = maxInlierDistMax;

        // choose samples along the circle and count inlier percentage
        for(float t =0; t<2*3.14159265359f; t+= 0.05f)
        {
            counter++;
            double cX = radius * Math.cos(t) + center.x;
            double cY = radius * Math.sin(t) + center.y;

            if(cX < dt.cols())
                if(cX >= 0)
                    if(cY < dt.rows())
                        if(cY >= 0)
                            if(dt.get((int)cY,(int)cX)[0] < maxInlierDist)
            {
                inlier++;
                inlierSet.add(new Point(cX,cY));
            }
        }

        return (float)inlier/counter;
    }

    float evaluateCircle(Mat dt, Point center, double radius)
    {

        float completeDistance = 0.0f;
        int counter = 0;

        float maxDist = 1.0f;   //TODO: this might depend on the size of the circle!

        float minStep = 0.001f;
        // choose samples along the circle and count inlier percentage

        //HERE IS THE TRICK that no minimum/maximum circle is used, the number of generated points along the circle depends on the radius.
        // if this is too slow for you (e.g. too many points created for each circle), increase the step parameter, but only by factor so that it still depends on the radius

        // the parameter step depends on the circle size, otherwise small circles will create more inlier on the circle
        double step = 2*3.14159265359 / (6.0f * radius);
        if(step < minStep) step = minStep; // TODO: find a good value here.

        //for(float t =0; t<2*3.14159265359f; t+= 0.05f) // this one which doesnt depend on the radius, is much worse!
        for(float t =0; t<2*3.14159265359f; t+= step)
        {
            double cX = radius * Math.cos(t) + center.x;
            double cY = radius * Math.sin(t) + center.y;

            if(cX < dt.cols())
                if(cX >= 0)
                    if(cY < dt.rows())
                        if(cY >= 0)
                            if(dt.get((int)cY,(int)cX)[0] <= maxDist)
            {
                completeDistance += dt.get((int)cY,(int)cX)[0];
                counter++;
            }

        }

        return counter;
    }


    void detectCircles(int numberOfCirclesToDetect)
    {
        //RANSAC

        Mat color = new Mat();
        Utils.bitmapToMat(this.image, color);

        // convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(color, gray, Imgproc.COLOR_BGR2GRAY);

        // get binary image
        Mat mask = new Mat();
        Core.compare(gray, new Scalar(0), mask, Core.CMP_GT);

        for(int j=0; j<numberOfCirclesToDetect; ++j)
        {
            ArrayList<Point> edgePositions;
            edgePositions = getPointPositions(mask);

            //std::cout << "number of edge positions: " << edgePositions.size() << std::endl;

            // create distance transform to efficiently evaluate distance to nearest edge
            Mat dt = new Mat();
            Core.subtract(mask, new Scalar(255), mask);
            Imgproc.distanceTransform(mask, dt, Imgproc.CV_DIST_L1, 3);

            int nIterations = 0;

            Point bestCircleCenter = new Point();
            double bestCircleRadius = 0.0;
            //float bestCVal = FLT_MAX;
            float bestCVal = -1;

            //float minCircleRadius = 20.0f; // TODO: if you have some knowledge about your image you might be able to adjust the minimum circle radius parameter.
            float minCircleRadius = 0.0f;

            //TODO: implement some more intelligent ransac without fixed number of iterations
            for(int i=0; i<2000; ++i)
            {
                //RANSAC: randomly choose 3 point and create a circle:
                //TODO: choose randomly but more intelligent,
                //so that it is more likely to choose three points of a circle.
                //For example if there are many small circles, it is unlikely to randomly choose 3 points of the same circle.
                int idx1 = (int)Math.random()%edgePositions.size();
                int idx2 = (int)Math.random()%edgePositions.size();
                int idx3 = (int)Math.random()%edgePositions.size();

                // we need 3 different samples:
                if(idx1 == idx2) continue;
                if(idx1 == idx3) continue;
                if(idx3 == idx2) continue;

                // create circle from 3 points:
                Point center; float radius;
                Circle circle;
                circle = getCircle(edgePositions.get(idx1), edgePositions.get(idx2), edgePositions.get(idx3));

                if(circle.radius < minCircleRadius)continue;


                //verify or falsify the circle by inlier counting:
                //float cPerc = verifyCircle(dt,center,radius, inlierSet);
                float cVal = evaluateCircle(dt,circle.center,circle.radius);

                if(cVal > bestCVal)
                {
                    bestCVal = cVal;
                    bestCircleRadius = circle.radius;
                    bestCircleCenter = circle.center;
                }

                ++nIterations;
            }
            //std::cout << "current best circle: " << bestCircleCenter << " with radius: " << bestCircleRadius << " and nInlier " << bestCVal << std::endl;
            Core.circle(color,bestCircleCenter,(int)bestCircleRadius,new Scalar(0,0,255));

            //TODO: hold and save the detected circle.

            //TODO: instead of overwriting the mask with a drawn circle it might be better to hold and ignore detected circles and dont count new circles which are too close to the old one.
            // in this current version the chosen radius to overwrite the mask is fixed and might remove parts of other circles too!

            // update mask: remove the detected circle!
            Core.circle(mask,bestCircleCenter, (int)bestCircleRadius, new Scalar(0), 10); // here the radius is fixed which isnt so nice.

            this.image = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(color, this.image);
        }
    }
}
