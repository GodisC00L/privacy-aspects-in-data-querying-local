public class BST<T> {
    private Node<T> root;

    public BST() {
        this.root = null;
    }

    public Node<T> getRoot() {
        return root;
    }

    private Node<T> addNodeRec(Node<T> current, double key, double timestamp, T values) {
        if(current == null) {
            return new Node<T>(key, timestamp, values);
        }
        if(key < current.getKey()) {
            current.setLeft(addNodeRec(current.getLeft(), key, timestamp, values));
        } else if (key > current.getKey()) {
            current.setRight(addNodeRec(current.getRight(), key, timestamp, values));
        } else {
            current.addValue(timestamp, values);
        }
        return current;
    }

    public void add(double key, double timestamp, T values) {
        root = addNodeRec(root, key, timestamp, values);
    }
}
