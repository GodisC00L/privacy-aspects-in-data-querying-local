import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class Database {
    private HashMap<Double, BST> db;
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

    void addToDB(double x, double velocity, double timestamp) {
        if (x > this.max_X || this.max_X == 0)
            this.max_X = x;
        else if (x < this.min_X || this.min_X == 0)
            this.min_X = x;
        if(db.containsKey(timestamp)) {
            db.get(timestamp).add(x, velocity);
        } else {
            db.put(timestamp, new BST(x, velocity));
        }
    }

    HashMap<Double, BST> getDb() {
        return db;
    }

    List<Double> getVelocityInRange(double timestamp, Pair<Double, Double> range) {
        List<Double> velociyList = new ArrayList<>();
        double upperBound = range.getP2(), lowerBound = range.getP1();
        BST relaventTree = db.get(timestamp);
        if(relaventTree == null) {
            System.out.println("No such timestamp in DB");
            return null;
        }

        Node splitNode = findSplitNode(relaventTree.getRoot(), lowerBound, upperBound);

        if(splitNode != null) {
            /* In case this is the only node in the range */
            velociyList.add(splitNode.getVelocity());
            Node currentNode  = splitNode.getLeft();
            /* Left subtree, path to lower bound */
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

            /* Right subtree, path to upper bound */
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
        System.out.println("SplitNode is null" + range.toString());
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
                if (currentNode == null) {
                    return null;
                }
            }
            return currentNode;
        }

        return null;
    }
}
