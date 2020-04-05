public class BST {
    private Node root;
    private double maxY, minY;

    public BST(){
        root = null;
    }

    public BST(DataFormat df){
        root = new Node(df);
    }

    public void insert(DataFormat df) {
        root = insert(root, df);
    }

    private Node insert(Node current, DataFormat df){
        if (current == null) {
            return new Node(df);
        }

        if(df.y < current.y) {
            current.left = insert(current.left, df);
        } else if (df.y > current.y) {
            current.right = insert(current.right, df);
        }
        return current;
    }

}
