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
    private final boolean DBG = false;

    private double min_X = 0, max_X = 0;

    private double min_Y = 0, max_Y = 0;

    double getMin_X() {
        return min_X;
    }

    double getMax_X() {
        return max_X;
    }

    public double getMin_Y() {
        return min_Y;
    }

    public double getMax_Y() {
        return max_Y;
    }

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


    /*Pair<Double, Double> getVelocityInRange(double timestamp, Pair<Double, Double> range) {


        /*DataArr relevantArr = db.get(timestamp);
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
        return new Pair<>(numOfElementsInRange, avgVelocity);*/
        /*return new Pair<>(-1.0,-1.0);
    }*/


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
