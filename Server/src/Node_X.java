/* Node Class for X axis */
public class Node_X {
    private double x_key;
    private Pair<Double, Double> possibleRange;
    private BST_Y y_tree;
    private Node_X left;
    private Node_X right;

    Node_X(double x_key, double y_key, double velocity) {
        this.x_key = x_key;
        possibleRange = new Pair<>(y_key, y_key);
        this.y_tree = new BST_Y(y_key, velocity);
        this.right = null;
        this.left = null;
    }

    public double getX_key() {
        return x_key;
    }

    public Pair<Double, Double> getPossibleRange() {
        return possibleRange;
    }

    public BST_Y getY_tree() {
        return y_tree;
    }

    public Node_X getLeft() {
        return left;
    }

    public void setLeft(Node_X left) {
        this.left = left;
    }

    public Node_X getRight() {
        return right;
    }

    public void setRight(Node_X right) {
        this.right = right;
    }

    public void setPossibleRangeMin(double min) {
        this.possibleRange.setP1(min);
    }

    public void setPossibleRangeMax(double max) {
        this.possibleRange.setP2(max);
    }

    public void setY_tree(double y_key, double velocity) {
        this.y_tree.add(y_key, velocity);
    }
}
