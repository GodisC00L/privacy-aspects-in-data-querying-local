/* Class that reads BIG data set file and retrieves from it the necessary data */

/* CURRENT TEST FILE IS koln-pruned.tr
 * the format of the file is:
 * the time (with 1-second granularity),
 * the vehicle identifier,
 * its position on the two-dimensional plane (x and y coordinates in meters)
 * and its speed (im meters per second).*/

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;


class WorkingWithDatasets {

    private Scanner datasetScanner;
    private BST tree = null;

    /* Constructor for class */
    WorkingWithDatasets(String path) throws FileNotFoundException, UnsupportedEncodingException {
        FileInputStream inputStream = new FileInputStream(path);
        datasetScanner = new Scanner(inputStream);
    }

    BST getDB() {
        if (tree == null)
            this.tree = createDB();
        return tree;
    }

    private class DataFormat  {
        private double timestamp, x, y, velocity;
        private int carID;

        DataFormat(double timestamp, int carID, double x, double y, double velocity) {
            this.timestamp = timestamp;
            this.x = x;
            this.y = y;
            this.velocity = velocity;
            this.carID = carID;
        }

        @Override
        public String toString() {
            return "dataFormat{" +
                    "timestamp=" + timestamp +
                    ", x=" + x +
                    ", y=" + y +
                    ", velocity=" + velocity +
                    ", carID=" + carID +
                    '}';
        }
    }

    private BST createDB() {
        BST<Pair> tree = new BST<>();
        String[] splitted;
        DataFormat df;
        Pair pair;
        while(datasetScanner.hasNextLine()) {
            splitted = datasetScanner.nextLine().split(" ");
            df = new DataFormat(Double.parseDouble(splitted[0]),
                    Integer.parseInt(splitted[1].contains("_") ? splitted[1].substring(13) : splitted[1]),
                    Double.parseDouble(splitted[2]),
                    Double.parseDouble(splitted[3]),
                    Double.parseDouble(splitted[4])
            );
            pair = new Pair(df.timestamp, df.velocity);
            tree.add(df.x, df.timestamp, pair);
        }
        datasetScanner.close();
        return tree;
    }

    public List<Double> getVelocityInRange(double timestamp, Pair<Double, Double> range) {
        List<Double> velociyList = new ArrayList<>();
        double uppperBound = range.getP2(), lowerBound = range.getP1();
        Node splitNode = findSplitNode(getDB().getRoot(), lowerBound, uppperBound);

        if(splitNode != null) {
            /* In case this is the only node in the range */
            splitNode.getValuesForTimestamp(timestamp, velociyList);
            Node currentNode  = splitNode.getLeft();
            /* Left subtree, path to lower bound */
            while(currentNode != null && !currentNode.isLeaf()) {
                if(lowerBound <= currentNode.getKey()) {
                    currentNode.getValuesForTimestamp(timestamp, velociyList);
                    if(currentNode.getRight() != null)
                        currentNode.getRight().getValuesForSubtree(timestamp, velociyList);
                    currentNode = currentNode.getLeft();
                } else {
                    currentNode = currentNode.getRight();
                }
            }
            if(currentNode != null && currentNode.getKey() >= lowerBound)
                currentNode.getValuesForTimestamp(timestamp, velociyList);

            /* Right subtree, path to upper bound */
            currentNode = splitNode.getRight();
            while(currentNode != null && !currentNode.isLeaf()) {
                if (currentNode.getKey() <= uppperBound) {
                    currentNode.getValuesForTimestamp(timestamp, velociyList);
                    if(currentNode.getLeft() != null)
                        currentNode.getLeft().getValuesForSubtree(timestamp, velociyList);
                    currentNode = currentNode.getRight();
                } else {
                    currentNode = currentNode.getLeft();
                }
            }
            if(currentNode != null && currentNode.getKey() <= uppperBound)
                currentNode.getValuesForTimestamp(timestamp, velociyList);
            return velociyList;
        }
        return null;
    }

    private Node findSplitNode(Node root, double lowerBound, double upperBound) {
        if (root != null) {
            Node currentNode = root;
            while (!currentNode.isLeaf() && (currentNode.getKey() > upperBound || currentNode.getKey() < lowerBound)) {
                if (currentNode.getKey() > upperBound)
                    currentNode = currentNode.getLeft();
                else
                    currentNode = currentNode.getRight();
            }
            return currentNode;
        }
        return null;
    }


    private void randomVelocities_andCutSize(int size) throws FileNotFoundException {
        String inputLine;
        String outputLine;
        String[] splited;
        double rand;
        long total=0;
        PrintWriter writer = new PrintWriter("Server/fixedVelocities_" + size + "_MB.txt");
        //PrintWriter writer = new PrintWriter("D:/fixedVelocities.txt");
        while(datasetScanner.hasNextLine() && bytesToMeg(total) <= size) {
            rand = ThreadLocalRandom.current().nextDouble(0, 30);
            inputLine = datasetScanner.nextLine();
            splited = inputLine.split(" ");
            splited[splited.length -1] =  String.format("%.2f", rand);
            outputLine = String.join(" ", splited);
            total += outputLine.getBytes(StandardCharsets.UTF_8).length;
            writer.println(outputLine);
        }
        writer.close();
    }

    private static final long  MEGABYTE = 1024L * 1024L;

    private static long bytesToMeg(long bytes) {
        return bytes / MEGABYTE ;
    }
}
