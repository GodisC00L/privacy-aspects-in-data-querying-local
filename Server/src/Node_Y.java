/* Node Class for Y axis */

import java.util.List;

class Node_Y {
    private double key;
    private double velocity;
    private Node_Y right;
    private Node_Y left;

    Node_Y(double key, double velocity) {
        this.key = key;
        this.velocity = velocity;
        this.right = null;
        this.left = null;
    }

    double getKey() {
        return key;
    }

    Node_Y getRight() {
        return right;
    }

    void setRight(Node_Y right) {
        this.right = right;
    }

    double getVelocity() {
        return velocity;
    }

    Node_Y getLeft() {
        return left;
    }

    void setLeft(Node_Y left) {
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
