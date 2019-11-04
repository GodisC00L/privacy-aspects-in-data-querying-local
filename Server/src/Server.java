import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class Server {
    private WorkingWithDatasets ds;
    private BST db;
    public final int k = 3;
    private final String path = "Server/fixedVelocities_20_MB_T1.txt";

    public Server() {
        try {
            ds = new WorkingWithDatasets(path);
            db = ds.getDB();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public BST getDb() {
        return db;
    }

    public double getAvgVelocity(Pair<Double, Double> range, double timestamp) {
        double res = -1;
        List<Double> allVelocities = ds.getVelocityInRange(timestamp, range);
        if(allVelocities.size() >= this.k) {
            res = 0;
            for(Double vel : allVelocities)
                res += vel;
            res /= allVelocities.size();
        }
        return res;
    }

    /*public static void main(String[] args) {
        Server server = new Server();
        Pair<Double, Double> range = new Pair<>(5214.0, 13700.0);
        double timestamp = 3646;
        Instant start = Instant.now();
        double avgVel = server.getAvgVelocity(range, timestamp);
        Instant end = Instant.now();
        System.out.println("Avg velocity for range: [" + range.getP1() + ", " + range.getP2() +"]\n"
                + "timestamp: " + timestamp
                + " is: " + avgVel + "\n" + Duration.between(start, end));
    }*/
}
