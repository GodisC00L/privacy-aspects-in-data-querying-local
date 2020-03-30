import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class Database {
    private HashMap<Double, DataArr> db;
    private final boolean DBG = false;

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
        DataArr relevantArr = db.get(timestamp);
        int lowerBoundIndex, upperBoundIndex;
        double avgVelocity;

        if (relevantArr == null) {
            System.out.println("No arr for timestamp: " + timestamp + " range: " + range.toString());
            return null;
        }

        if(range.getP1() == this.min_X) lowerBoundIndex = 0;
        else lowerBoundIndex = relevantArr.closestNumber(range.getP1(), false);
        if (lowerBoundIndex == -1) {
            if (DBG)
                System.out.println("No index for lowerBound: " + range.getP1());
            return null;
        }

        if(range.getP2() == this.max_X) upperBoundIndex = relevantArr.size()-1;
        else upperBoundIndex = relevantArr.closestNumber(range.getP2(), true);
        if (upperBoundIndex == -1) {
            if(DBG)
                System.out.println("No index for upperBound: " + range.getP2());
            return null;
        }

        double numOfElementsInRange = upperBoundIndex - lowerBoundIndex + 1;
        if(numOfElementsInRange == 1) avgVelocity = relevantArr.get(upperBoundIndex).velocity;
        else {
            if (lowerBoundIndex != 0)
                avgVelocity = (relevantArr.get(upperBoundIndex).sumToIndex -
                        relevantArr.get(lowerBoundIndex - 1).sumToIndex) / numOfElementsInRange;
            else
                avgVelocity = relevantArr.get(upperBoundIndex).sumToIndex / numOfElementsInRange;
        }
        return new Pair<>(numOfElementsInRange, avgVelocity);
    }

    void addSumToIndexForDb() {
        DecimalFormat df2 = new DecimalFormat("#.##");
        for (DataArr dataArr : db.values()) {
            for (int i = 1; i < dataArr.size(); i++) {
                dataArr.get(i).sumToIndex += dataArr.get(i - 1).sumToIndex;
                dataArr.get(i).sumToIndex = (Double.parseDouble(df2.format(dataArr.get(i).sumToIndex)));
            }
        }
    }

}
