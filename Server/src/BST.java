class BST {
    private Node root;

    BST(double key, double velocity) {
        this.root = addNodeRec(null, key, velocity);
    }

    Node getRoot() {
        return root;
    }

    private Node addNodeRec(Node current, double key, double velocity) {
        if(current == null) {
            return new Node(key, velocity);
        }
        if(key < current.getKey()) {
            current.setLeft(addNodeRec(current.getLeft(), key, velocity));
        } else if (key > current.getKey()) {
            current.setRight(addNodeRec(current.getRight(), key, velocity));
        }
        return current;
    }

    void add(double key, double velocity) {
        root = addNodeRec(root, key, velocity);
    }
}
