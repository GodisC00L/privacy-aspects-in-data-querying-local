/* Generic node class */

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class Node {
    private double key;
    private double velocity;
    private Node right;
    private Node left;

    Node(double key, double velocity) {
        this.key = key;
        this.velocity = velocity;
        this.right = null;
        this.left = null;
    }

    double getKey() {
        return key;
    }

    Node getRight() {
        return right;
    }

    Node getLeft() {
        return left;
    }

    double getVelocity() {
        return velocity;
    }

    void setRight(Node right) {
        this.right = right;
    }

    void setLeft(Node left) {
        this.left = left;
    }

    boolean isLeaf() {
        return (this.left == null && this.right == null);
    }

    void getValuesForSubtree(List<Double> list) {
        if(getLeft() != null)
            getLeft().getValuesForSubtree(list);
        if(getRight() != null)
            getRight().getValuesForSubtree(list);
        list.add(this.getVelocity());
    }
}
