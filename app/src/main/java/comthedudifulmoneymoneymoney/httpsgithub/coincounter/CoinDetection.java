package comthedudifulmoneymoneymoney.httpsgithub.coincounter;

import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CoinDetection {

    private static final String TAG = "COIN";

    // Array met gedetecteerde cirkels
    float[][] circles;

    // Geldwaarde per circle
    float[] circle_value;

    ThreadPoolExecutor threadPool;

    public CoinDetection(float[][] circles) {
        this.circles = circles;
        circle_value = new float[circles.length];
        makeThreadPool();
    }

    // Bepaal waarden van circles door middel van diameter analyse
    public void ValueCircles_by_radius() {

        Log.i(TAG, "DIAMETERANALYSE BEGONNEN");

        int i;
        // Loop door circles
        for (i = 0; i < circles.length; i++) {

            final int ii = i;

            threadPool.execute(new Runnable() {
                float[] circle_value = CoinDetection.this.circle_value;
                float[][] circles = CoinDetection.this.circles;

                int i = ii;

                @Override
                public void run() {
                    // Als de waarde van de cirkel nog niet bepaald is
                    if (circle_value[i] == 0.0f) {

                        // Bepaal waarde cirkel op basis van "voting" dmv vergelijken diameters
                        int[] votes = new int[6];

                        // TODO: THREADING TOEVOEGEN

                        // Loop door overige cirkels
                        for (int j = 0; j < this.circles.length; j++) {

                            // Niet vergelijken met zichzelf
                            if (j != i) {

                                double epsilon = 0.015;

                                for (int a = 0; a < CoinRatios.vijfcent.length; a++) {
                                    // Als de diameter deling significant lijkt op die van vaste verhouding
                                    if (Math.abs(CoinRatios.vijfcent[a] - ((double) this.circles[i][2]
                                            / (double) this.circles[j][2])) < (CoinRatios.vijfcent[a] * epsilon)) {
                                        votes[0] += 1;
                                        break;
                                    }
                                }

                                for (int a = 0; a < CoinRatios.tiencent.length; a++) {
                                    // Als de diameter deling significant lijkt op die van vaste verhouding
                                    if (Math.abs(CoinRatios.tiencent[a] - ((double) this.circles[i][2]
                                            / (double) this.circles[j][2])) < (CoinRatios.tiencent[a] * epsilon)) {
                                        votes[1] += 1;
                                        break;
                                    }
                                }

                                for (int a = 0; a < CoinRatios.twintigcent.length; a++) {
                                    // Als de diameter deling significant lijkt op die van vaste verhouding
                                    if (Math.abs(CoinRatios.twintigcent[a] - ((double) this.circles[i][2]
                                            / (double) this.circles[j][2])) < (CoinRatios.twintigcent[a] * epsilon)) {
                                        votes[2] += 1;
                                        break;
                                    }
                                }

                                for (int a = 0; a < CoinRatios.vijftigcent.length; a++) {
                                    // Als de diameter deling significant lijkt op die van vaste verhouding
                                    if (Math.abs(CoinRatios.vijftigcent[a] - ((double) this.circles[i][2]
                                            / (double) this.circles[j][2])) < (CoinRatios.vijftigcent[a] * epsilon)) {
                                        votes[3] += 1;
                                        break;
                                    }
                                }

                                for (int a = 0; a < CoinRatios.euro.length; a++) {
                                    // Als de diameter deling significant lijkt op die van vaste verhouding
                                    if (Math.abs(CoinRatios.euro[a] - ((double) this.circles[i][2]
                                            / (double) this.circles[j][2])) < (CoinRatios.euro[a] * epsilon)) {
                                        votes[4] += 1;
                                        break;
                                    }
                                }

                                for (int a = 0; a < CoinRatios.tweeeuro.length; a++) {
                                    // Als de diameter deling significant lijkt op die van vaste verhouding
                                    if (Math.abs(CoinRatios.tweeeuro[a] - ((double) this.circles[i][2]
                                            / (double) this.circles[j][2])) < (CoinRatios.tweeeuro[a] * epsilon)) {
                                        votes[5] += 1;
                                        break;
                                    }
                                }
                            }
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

                        // Geef waarde aan de munt
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
            });
        }
    }

    private void makeThreadPool() {
        int numProcs = Runtime.getRuntime().availableProcessors();
        LinkedBlockingQueue<Runnable> linkedBQ = new LinkedBlockingQueue<Runnable>();
        threadPool = new ThreadPoolExecutor(numProcs, numProcs, 1, TimeUnit.SECONDS, linkedBQ);
    }
}
