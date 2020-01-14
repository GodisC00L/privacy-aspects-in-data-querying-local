import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class Database {
    private HashMap<Double, DataArr> db;


    /* ============================================ */
    //private HashMap<Double, BST> db;
    private double min_X = 0, max_X = 0;

    double getMin_X() {
        return min_X;
    }

    double getMax_X() {
        return max_X;
    }

    Database() {
        this.db = new HashMap<>();
    }

    void addToDB(DataFormat dataFormat) {
        if (dataFormat.x > this.max_X || this.max_X == 0)
            this.max_X = dataFormat.x;
        else if (dataFormat.x < this.min_X || this.min_X == 0)
            this.min_X = dataFormat.x;
        if(db.containsKey(dataFormat.timestamp)) {
            db.get(dataFormat.timestamp).add(dataFormat);
        } else {
            db.put(dataFormat.timestamp, new DataArr(dataFormat));
        }
    }

    HashMap<Double, DataArr> getDb() {
        return db;
    }

    Pair<Double, Double> getVelocityInRange(double timestamp, Pair<Double, Double> range) {
        Pair<Double, Double> returnPair = new Pair<>();
        //double upperBound = range.getP2(), lowerBound = range.getP1();
        DataArr relaventArr = db.get(timestamp);

        if (relaventArr == null) {
            System.out.println("No arr for timestamp: " + timestamp + " range: " + range.toString());
            return null;
        }

        int lowerBoundIndex = relaventArr.indexOfX(range.getP1());
        if (lowerBoundIndex == -1) {
            System.out.println("No index for lowerBound: " + range.getP1());
            return null;
        }

        int upperBoundIndex = relaventArr.indexOfX(range.getP2());
        if (upperBoundIndex == -1) {
            System.out.println("No index for upperBound: " + range.getP2());
            return null;
        }

        double k = upperBoundIndex - lowerBoundIndex;
        double avgVelocity = (relaventArr.get(upperBoundIndex).sumToIndex - relaventArr.get(lowerBoundIndex).sumToIndex) / k;
        return new Pair<>(k, avgVelocity);
    }
}
