import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    List<Double> getVelocityInRange(double timestamp, Pair<Double, Double> range) {
        List<Double> velociyList = new ArrayList<>();
        double upperBound = range.getP2(), lowerBound = range.getP1();
        return null;
        /*BST relaventTree = db.get(timestamp);
        if(relaventTree == null) {
            System.out.println("No such timestamp in DB");
            return null;
        }

        Node splitNode = findSplitNode(relaventTree.getRoot(), lowerBound, upperBound);

        if(splitNode != null) {
            *//* In case this is the only node in the range *//*
            velociyList.add(splitNode.getVelocity());
            Node currentNode  = splitNode.getLeft();
            *//* Left subtree, path to lower bound *//*
            while(currentNode != null && !currentNode.isLeaf()) {
                if(lowerBound <= currentNode.getKey()) {
                    velociyList.add(currentNode.getVelocity());
                    if(currentNode.getRight() != null)
                        currentNode.getRight().getValuesForSubtree(velociyList);
                    currentNode = currentNode.getLeft();
                } else {
                    currentNode = currentNode.getRight();
                }
            }
            if(currentNode != null && currentNode.getKey() >= lowerBound)
                velociyList.add(currentNode.getVelocity());

            *//* Right subtree, path to upper bound *//*
            currentNode = splitNode.getRight();
            while(currentNode != null && !currentNode.isLeaf()) {
                if (currentNode.getKey() <= upperBound) {
                    velociyList.add(currentNode.getVelocity());
                    if(currentNode.getLeft() != null)
                        currentNode.getLeft().getValuesForSubtree(velociyList);
                    currentNode = currentNode.getRight();
                } else {
                    currentNode = currentNode.getLeft();
                }
            }
            if(currentNode != null && currentNode.getKey() <= upperBound)
                velociyList.add(currentNode.getVelocity());
            return velociyList;
        }
        return null;*/
    }

    private Node findSplitNode(Node root, double lowerBound, double upperBound) {
        if (root != null) {
            Node currentNode = root;
            while (!currentNode.isLeaf() && (currentNode.getKey() > upperBound || currentNode.getKey() < lowerBound)) {
                if (currentNode.getKey() > upperBound)
                    currentNode = currentNode.getLeft();
                else
                    currentNode = currentNode.getRight();
                if (currentNode == null)
                    return null;
            }
            return currentNode;
        }
        return null;
    }

}
