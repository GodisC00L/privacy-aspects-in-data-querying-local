public class BST_X {
    private Node_X root;

    BST_X(double x_key, double y_key, double velocity) {
        this.root = addNodeRec(null,x_key, y_key, velocity);
    }

    Node_X getRoot() {
        return root;
    }

    private Node_X addNodeRec(Node_X current, double x_key, double y_key, double velocity) {
        if(current == null) {
            return new Node_X(x_key, y_key, velocity);
        }
        if(x_key < current.getX_key()) {
            current.setLeft(addNodeRec(current.getLeft(),x_key, y_key, velocity));
        } else if (x_key > current.getX_key()) {
            current.setRight(addNodeRec(current.getRight(),x_key, y_key, velocity));
        } else {
            updateRange(current, y_key);
            current.setY_tree(y_key, velocity);
        }
        return current;
    }

    void add(double x_key, double y_key, double velocity) {
        root = addNodeRec(root,x_key, y_key, velocity);
    }

    void updateRange(Node_X current, double y_key) {
        if(y_key < current.getPossibleRange().getP1())
            current.setPossibleRangeMin(y_key);
        else if (y_key > current.getPossibleRange().getP2())
            current.setPossibleRangeMax(y_key);
    }

}
