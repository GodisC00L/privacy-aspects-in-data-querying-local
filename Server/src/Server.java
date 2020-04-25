import java.io.File;
import java.io.IOException;

public class Server {
    private Database db;
    public int k = 10;
    //private final String path = "Server/koln.tr";

    public Server() {
        try {
            String targetListPath = "Server/fixedVelocities_40_MB_target.csv";
            //String path = "Server/fixedVelocities_40_MB.txt";
            File path = new File("Server/fixedVelocities_40_MB.txt");
            WorkingWithDatasets ds = new WorkingWithDatasets(path, targetListPath);
            long startTime = System.nanoTime();
            db = ds.getDB(path.length());
            double dbBuildTime = (System.nanoTime() - startTime) / 1e9;
            System.out.println("================================" +
                    "\nDB created Successfully!\nK value is: " + k
                    + "\nMin X: " + db.min_X + "\nMax X: " + db.max_X
                    + "\nMin y: " + db.min_Y + "\nMax y: " + db.max_Y
                    + "\nBuild time: " + dbBuildTime + "[sec]\n"
                    + "================================\n");
        } catch (IOException e) {
            System.out.println("Error on creating DB, Bad file path!");
            e.printStackTrace();
        }
    }

    double getMin_X() { return db.min_X; }

    double getMax_X() {
        return db.max_X;
    }

    double getMin_Y() { return db.min_Y; }

    double getMax_Y() {
        return db.max_Y;
    }

    public double getAvgVelocity2D(Pair<Pair<Double,Double>, Pair<Double,Double>> area, double timestamp) {
        double result = -1;
        if (!isInRange(area)) {
            return result;
        }
        Pair<Double, Double> xRange = new Pair<>(area.getP1().getP1(), area.getP2().getP1());

        Pair<Double, Double> yRange = new Pair<>(area.getP1().getP2(), area.getP2().getP2());
        if (yRange.getP1() > yRange.getP2()){
            yRange.swap();
        }
        Pair<Double, Integer> velocityNumOfElements = db.getVelocityInRange(timestamp, new Pair<>(xRange, yRange));
        if(velocityNumOfElements != null && velocityNumOfElements.getP2() >= this.k)
            result =  velocityNumOfElements.getP1();
        return result;
    }

    public double getMaxVel(double timestamp, Pair<Double, Double> xRange) {
        double ret = -1;
        if(!isInRangeX(xRange.getP1()) || !isInRangeX(xRange.getP2())) {
            return ret;
        }
        return db.getMaxVel(timestamp, xRange);
    }

    public double getMinVel(double timestamp, Pair<Double, Double> xRange) {
        double ret = -1;
        if(!isInRangeX(xRange.getP1()) || !isInRangeX(xRange.getP2())) {
            return ret;
        }
        return db.getMinVel(timestamp, xRange);
    }

    boolean isInRange(Pair<Pair<Double, Double>, Pair<Double, Double>> area) {
        return isInRangeX(area.getP1().getP1()) && isInRangeY(area.getP1().getP2())
                && isInRangeX(area.getP2().getP1()) && isInRangeY(area.getP2().getP2());
    }

    boolean isInRangeX(double x) {
        return x <= db.max_X && x >= db.min_X;
    }

    boolean isInRangeY(double y) {
        return y <= db.max_Y && y >= db.min_Y;
    }

    public void setK(int k) {
        this.k = k;
    }

    public int getNumOfVehicles(double timestamp){ return db.getDb().get(timestamp).getNumOfVehicles(); }

    public static void main(String[] args) {
        Server srv = new Server();
    }
}
