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
        Pair<Double, Double> kVelocityPair = db.getVelocityInRange(timestamp, range);
        if(kVelocityPair != null && kVelocityPair.getP1() >= this.k)
            result =  kVelocityPair.getP2();
        return result;
    }

    public void setK(int k) {
        this.k = k;
    }
}
