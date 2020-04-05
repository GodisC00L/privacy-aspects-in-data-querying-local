import javafx.beans.binding.DoubleExpression;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class Server {
    private WorkingWithDatasets ds;
    private Database db;
    private final String targetListPath = "Server/fixedVelocities_40_MB_target.csv";

    private final String path = "Server/fixedVelocities_40_MB.txt";
    public int k = 10;
    //private final String path = "/home/user/Downloads/koln.tr";

    public Server() {
        try {
            ds = new WorkingWithDatasets(path, targetListPath);
            long startTime = System.nanoTime();
            db = ds.getDB();
            double dbBuildTime = (System.nanoTime() - startTime) / 1e9;
            System.out.println("================================" +
                    "\nDB created Successfully!\nK value is: " + k
                    + "\nMin X: " + db.getMin_X() + "\nMax X: " + db.getMax_X()
                    + "\nBuild time: " + dbBuildTime + "[sec]\n"
                    + "================================\n");
        } catch (IOException e) {
            System.out.println("Error on creating DB, Bad file path!");
            e.printStackTrace();
        }
    }

    public Database getDb() {
        return db;
    }

    public double getAvgVelocity(Pair<Double, Double> range, double timestamp) {
        double result = -1;
        if(range.getP1() < db.getMin_X() || range.getP2() > db.getMax_X())
            return result;
        //Pair<Double, Double> kVelocityPair = db.getVelocityInRange(timestamp, range);
        /*if(kVelocityPair != null && kVelocityPair.getP1() >= this.k)
            result =  kVelocityPair.getP2();*/
        return result;
    }

    public double getAvgVelocity2D(Pair<Pair<Double, Double>, Pair<Double, Double>> area, double timestamp) {
        double result = -1;
        if (!isInRange(area)) {
            return result;
        }
        Pair<Double, Double> xRange = new Pair<>(area.getP1().getP1(), area.getP2().getP1());
        if (xRange.getP1() > xRange.getP2()){
            xRange.swap();
        }
        Pair<Double, Double> yRange = new Pair<>(area.getP1().getP2(), area.getP2().getP2());
        if (yRange.getP1() > yRange.getP2()){
            yRange.swap();
        }
        Pair<Double, Integer> velocityNumOfElements = db.getVelocityInRange(timestamp, new Pair<>(xRange, yRange));
        if(velocityNumOfElements != null && velocityNumOfElements.getP2() >= this.k)
            result =  velocityNumOfElements.getP1();
        return result;
    }

    boolean isInRange(Pair<Pair<Double, Double>, Pair<Double, Double>> area) {
        return isInRangeX(area.getP1().getP1()) && isInRangeY(area.getP1().getP2())
                && isInRangeX(area.getP2().getP1()) && isInRangeY(area.getP2().getP2());
    }

    boolean isInRangeX(double x) {
        return x <= db.getMax_X() && x >= db.getMin_X();
    }

    boolean isInRangeY(double y) {
        return y <= db.getMax_Y() && y >= db.getMin_Y();
    }

    public void setK(int k) {
        this.k = k;
    }

    public static void main(String[] args) {
        Server srv = new Server();
        srv.setK(1);
        Pair<Double, Double> p1 = new Pair<>(1.6, 123121.);
        Pair<Double, Double> p2 = new Pair<>(1.6, 123121.);

        System.out.println(srv.getAvgVelocity2D(new Pair<>(p1, p2),1000));
    }
}
