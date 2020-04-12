import java.util.HashMap;


/**
 * Database structure:
 *      HashMap: Key:       Double Timestamp
 *               Value:     BST yTs
 *
 *      BST:     Node:      double Y
*                           DataArr xList
 */

class Database {
    private HashMap<Double, BST> db;

    double min_X = 0, max_X = 0;
    double min_Y = 0, max_Y = 0;

    Database() {
        this.db = new HashMap<>();
    }

    void addToDB(DataFormat dataFormat) {
        updateMinMax(dataFormat);
        BST tsRoot = db.getOrDefault(dataFormat.timestamp, null);

        if(tsRoot != null) {
            tsRoot.insert(dataFormat);
        } else {
            tsRoot = new BST(dataFormat);
            db.put(dataFormat.timestamp, tsRoot);
        }
    }

    private void updateMinMax(DataFormat dataFormat) {
        if (dataFormat.x > this.max_X || this.max_X == 0)
            this.max_X = dataFormat.x;
        if (dataFormat.x < this.min_X || this.min_X == 0)
            this.min_X = dataFormat.x;
        if (dataFormat.y > this.max_Y || this.max_Y == 0)
            this.max_Y = dataFormat.y;
        if (dataFormat.y < this.min_Y || this.min_Y == 0)
            this.min_Y = dataFormat.y;
    }

    HashMap<Double, BST> getDb() {
        return db;
    }

    void balanceBST() {
        for(BST yTree : db.values()) {
            yTree.balance();
            yTree.addSumAndMergeLists();
        }
    }

    /*
     * 1. Find first y BST in Y range.
     * 2. Find indexes of xList that fit to X range
     * 3. Go through sub xList:
     *      3.1 if Y in Y range:
     *          sum Velocity and increment counter
     * 4. check if couter fits k
     * 5. return */
    Pair<Double, Integer> getVelocityInRange(double timestamp, Pair<Pair<Double, Double>, Pair<Double, Double>> ranges) {
        BST relevantYBst = db.get(timestamp);
        Pair<Double, Double> yRange = ranges.getP2();
        Pair<Double, Double> xRange = ranges.getP1();

        Node relevandSubTree = relevantYBst.getRelevantSubTree(yRange);
        Pair<Integer, Integer> subXListIndexs = getSubXList(xRange, relevandSubTree.xList);

        int counter = 0;
        double avgVelocity = 0;

        for (int i = subXListIndexs.getP1(); i <= subXListIndexs.getP2(); i++) {
            if(isInRange(yRange, relevandSubTree.xList.get(i).y)) {
                counter++;
                avgVelocity += relevandSubTree.xList.get(i).velocity;
            }
        }
        if(counter == 0)
            return new Pair<>(-1.0, counter);
        return new Pair<>(avgVelocity/counter, counter);
    }

    Pair<Integer, Integer> getSubXList(Pair<Double, Double> xRange, DataArr dataArr) {
        Pair<Integer, Integer> result = new Pair<>(-1, -1);
        result.setP1(dataArr.closestNumber(xRange.getP1(), false));
        result.setP2(dataArr.closestNumber(xRange.getP2(), true));
        return result;
    }

    boolean isInRange(Pair<Double, Double> range, double test){
        return test <= range.getP2() && test >= range.getP1();
    }

}
