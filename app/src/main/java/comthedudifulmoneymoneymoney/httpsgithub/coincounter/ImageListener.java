/*
 * Framework code written for the Multimedia course taught in the first year
 * of the UvA Informatica bachelor.
 *
 * Nardi Lam, 2015 (based on code by I.M.J. Kamps, S.J.R. van Schaik, R. de Vries, 2013)
 */

package comthedudifulmoneymoneymoney.httpsgithub.coincounter;

import android.graphics.Bitmap;

/*
 * This is a simple generic interface for classes that can recieve images from ImageSources.
 */
public interface ImageListener {
    void onImage(Bitmap argb);
}
