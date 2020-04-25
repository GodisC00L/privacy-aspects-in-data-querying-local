/*
 * Copyright (c) 2020.
 * Created by Edan Shamai and Natanel Ziv
 * Based on the work of Mr. Eyal Nussbaum and Prof. Michael Segal
 */

import java.util.HashMap;

/**
 * Handle database creation and management.
 *
 * Database structure:
 *      HashMap: Key:       Double Timestamp
 *               Value:     BST yPerTs
 *
 *      BST:     double Y
 *               Node right
 *               Node left
 *               DataArr xList
 */
class Database {
    private HashMap<Double, BST> db;

    double min_X = 0, max_X = 0;
    double min_Y = 0, max_Y = 0;

    /**
     * Database constructor
     * creates new HashMap
     */
    Database() {
        this.db = new HashMap<>();
    }

    /**
     * Adds new element to the database while maintaining global variables
     *
     * @param dataFormat - object with all the information
     */
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

    /**
     * Update Min / Max global X and Y values
     *
     * @param dataFormat - DataFormat object
     */
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

    /**
     * Getter for database
     *
     * @return database
     */
    HashMap<Double, BST> getDb() {
        return db;
    }

    /**
     * Balance BST tree
     */
    void balanceBST() {
        long size = db.size();
        long num = 0;
        System.out.println("\nStart Balancing...");
        for(BST yTree : db.values()) {
            num++;
            yTree.balance();
            yTree.addSumAndMergeLists();
            int precent = (int)((num * 100)/size);
            printProgressBar(precent);
        }
    }

    /**
     * Finds the average velocity in the given area
     * Algorithm:
     *      1. Find first y_BST for given timestamp - relevantYBst.
     *      1. Find first node in relevantYBst for given Y values - relevantSubTree.
     *      2. Find indexes of xList that fit to X values - subXListIndexes.
     *      3. Go through sub xList in range of subXListIndexes:
     *           3.1 if y of the dataFormat element in the array is in Y range:
     *               sum average velocity and increment counter
     *      4. If counter is 0 -> set average velocity -1
     *
     * @param timestamp - requested timestamp as double.
     * @param area      - expressed as (x1, x2), (y1, y2)
     * @return average velocity and counter.
     */
    Pair<Double, Integer> getVelocityInRange(double timestamp, Pair<Pair<Double, Double>, Pair<Double, Double>> area) {
        BST relevantYBst = db.get(timestamp);
        Pair<Double, Double> yRange = area.getP2();
        Pair<Double, Double> xRange = area.getP1();

        Node relevantSubTree = relevantYBst.getRelevantSubTree(yRange);
        Pair<Integer, Integer> subXListIndexes = getSubXList(xRange, relevantSubTree.xList);

        int counter = 0;
        double avgVelocity = 0;

        for (int i = subXListIndexes.getP1(); i <= subXListIndexes.getP2(); i++) {
            if(isInRange(yRange, relevantSubTree.xList.get(i).y)) {
                counter++;
                avgVelocity += relevantSubTree.xList.get(i).velocity;
            }
        }
        if(counter == 0)
            return new Pair<>(-1.0, counter);
        return new Pair<>(avgVelocity/counter, counter);
    }

    /**
     * Finds the indexes of the relevant x values in x data array.
     *
     * @param xRange - values as (x1, x2)
     * @param dataArr - X array with DataFormat objects as elements
     * @return pair of indexes fits the requested range. If no such indexes return (-1, -1)
     */
    Pair<Integer, Integer> getSubXList(Pair<Double, Double> xRange, DataArr dataArr) {
        Pair<Integer, Integer> result = new Pair<>(-1, -1);
        result.setP1(dataArr.closestNumber(xRange.getP1(), false));
        result.setP2(dataArr.closestNumber(xRange.getP2(), true));
        return result;
    }

    /**
     * Check if given test value is in range.
     *
     * @param range - desired range
     * @param test  - target value
     * @return true if in range, otherwise false
     */
    boolean isInRange(Pair<Double, Double> range, double test){
        return test <= range.getP2() && test >= range.getP1();
    }

    /**
     * Finds and return the maximum velocity in a given range
     * @param timestamp
     * @param xRange
     * @return maxVelocity
     */
    public double getMaxVel(double timestamp, Pair<Double, Double> xRange) {
        BST relevantTree = db.get(timestamp);
        DataArr xList = relevantTree.getRoot().xList;
        Pair<Integer, Integer> relevantIndexes = getSubXList(xRange, xList);
        double maxVelocity = -1;

        for(int i = relevantIndexes.getP1(); i <= relevantIndexes.getP2(); i++) {
            if(xList.get(i).velocity > maxVelocity) {
                maxVelocity = xList.get(i).velocity;
            }
        }
        return maxVelocity;
    }


    /**
     * Finds and return the minimum velocity in a given range
     * @param timestamp
     * @param xRange
     * @return maxVelocity
     */
    public double getMinVel(double timestamp, Pair<Double, Double> xRange) {
        BST relevantTree = db.get(timestamp);
        DataArr xList = relevantTree.getRoot().xList;
        Pair<Integer, Integer> relevantIndexes = getSubXList(xRange, xList);
        double minVelocity = -1;

        for(int i = relevantIndexes.getP1(); i <= relevantIndexes.getP2(); i++) {
            if(xList.get(i).velocity < minVelocity) {
                minVelocity = xList.get(i).velocity;
            }
        }
        return minVelocity;
    }


    public static void printProgressBar(int percent){
        StringBuilder bar = new StringBuilder("[");
        for(int i = 0; i < 50; i++){
            if( i < (percent/2)){
                bar.append("=");
            } else if( i == (percent/2)) {
                bar.append(">");
            } else {
                bar.append(" ");
            }
        }


        bar.append("]   ").append(percent).append("%     ");
        System.out.print("\r" + bar.toString());
        if (percent == 100)
            System.out.println('\n');
    }
}
