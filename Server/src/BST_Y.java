class BST_Y {
    private Node_Y root;

    BST_Y(double y_key, double velocity) {
        this.root = addNodeRec(null, y_key, velocity);
    }

    Node_Y getRoot() {
        return root;
    }

    private Node_Y addNodeRec(Node_Y current, double y_key, double velocity) {
        if(current == null) {
            return new Node_Y(y_key, velocity);
        }
        if(y_key < current.getKey()) {
            current.setLeft(addNodeRec(current.getLeft(), y_key, velocity));
        } else if (y_key > current.getKey()) {
            current.setRight(addNodeRec(current.getRight(), y_key, velocity));
        }
        return current;
    }

    void add(double y_key, double velocity) {
        root = addNodeRec(root, y_key, velocity);
    }
}
