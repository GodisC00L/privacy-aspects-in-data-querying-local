import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class Server {
    private WorkingWithDatasets ds;
    private Database db;
    public final int k = 3;
    private final String path = "Server/fixedVelocities_20_MB_T1.txt";
    //private final String path = "Server/t1.txt";

    public Server() {
        try {
            ds = new WorkingWithDatasets(path);
            long startTime = System.nanoTime();
            db = ds.getDB();
            double dbBuildTime = (System.nanoTime() - startTime) / 1e9;
            System.out.println("================================" +
                    "\nDB created Successfully!\nK value is: " + k
                    + "\nMax X: " + db.getMax_X() + "\nMin X: " + db.getMin_X()
                    + "\nBuild time: " + dbBuildTime + "[sec]\n"
                    + "================================\n");
        } catch (FileNotFoundException e) {
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
        List<Double> allVelocities = db.getVelocityInRange(timestamp, range);
        if(allVelocities.size() >= this.k) {
            result = 0;
            for(Double vel : allVelocities)
                result += vel;
            result /= allVelocities.size();
        }
        return result;
    }
}
