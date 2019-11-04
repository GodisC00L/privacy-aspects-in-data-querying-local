import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class Server {
    private WorkingWithDatasets ds;
    private BST db;
    private final int k = 4;
    //private final String path = "Server/fixedVelocities_10_MB.txt";
    private final String path = "D:/koln-pruned.tr";

    public Server() {
        try {
            ds = new WorkingWithDatasets(path);
            //db = ds.getDB();
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
        System.out.println("List Size: " + allVelocities.size());
        if(allVelocities.size() >= this.k) {
            res = 0;
            for(Double vel : allVelocities)
                res += vel;
            res /= allVelocities.size();
        }
        return res;
    }

    private void saveDbToFile(BST db) throws IOException {
        OutputStream file = new FileOutputStream( "tree.bin" );
        OutputStream buffer = new BufferedOutputStream( file );
        ObjectOutput output = new ObjectOutputStream( buffer );

        output.writeObject(db);
    }

    public static void main(String[] args) {
        Server server = new Server();
        /*try {
            server.saveDbToFile(server.getDb());
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        /*Pair<Double, Double> range = new Pair<>(5214.0, 13700.0);
        double timestamp = 3646;
        Instant start = Instant.now();
        double avgVel = server.getAvgVelocity(range, timestamp);
        Instant end = Instant.now();
        System.out.println("Avg velocity for range: [" + range.getP1() + ", " + range.getP2() +"]\n"
                + "timestamp: " + timestamp
                + " is: " + avgVel + "\n" + Duration.between(start, end));*/
    }
}
