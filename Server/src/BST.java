import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Vector;

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
            minY = df.y;
            maxY = df.y;
            return new Node(df);
        }

        if(df.y < current.y) {
            current.left = insert(current.left, df);
        } else if (df.y > current.y) {
            current.right = insert(current.right, df);
        } else {
            current.xList.add(df);
        }
        return current;
    }

    /* This function traverse the skewed binary tree and
       stores its nodes pointers in vector nodes[] */
    void storeBSTNodes(Node root, Vector<Node> nodes) {
        // Base case
        if (root == null)
            return;

        // Store nodes in Inorder (which is sorted
        // order for BST)
        storeBSTNodes(root.left, nodes);
        nodes.add(root);
        storeBSTNodes(root.right, nodes);
    }

    /* Recursive function to construct binary tree */
    Node buildTreeUtil(Vector<Node> nodes, int start, int end) {
        // base case
        if (start > end)
            return null;

        /* Get the middle element and make it root */
        int mid = (start + end) / 2;
        Node node = nodes.get(mid);

        /* Using index in Inorder traversal, construct
           left and right subtress */
        node.left = buildTreeUtil(nodes, start, mid - 1);
        node.right = buildTreeUtil(nodes, mid + 1, end);

        return node;
    }

    // This functions converts an unbalanced BST to
    // a balanced BST
    Node buildTree(Node root) {
        // Store nodes of given BST in sorted order
        Vector<Node> nodes = new Vector<Node>();
        storeBSTNodes(root, nodes);

        // Constucts BST from nodes[]
        int n = nodes.size();
        return buildTreeUtil(nodes, 0, n - 1);
    }

    void balance(){
        root = buildTree(root);
    }

    void addSumAndMergeLists(){
        addSumToIndexX(root);
    }

    void addSumToIndexX(Node node) {
        if (node == null)
            return;

        // first recur on left subtree
        addSumToIndexX(node.left);

        // then recur on right subtree
        addSumToIndexX(node.right);

        // now deal with the node
        Collections.sort(node.xList);
        addSumToIndexXInner(node.xList);

        if(!node.isLeaf()) {
            if(node.left != null) {
                node.xList.addAll(node.left.xList);
                updateMinMaxY(node.left.y);
            }
            if(node.right != null) {
                node.xList.addAll(node.right.xList);
                updateMinMaxY(node.right.y);
            }
            Collections.sort(node.xList);
        }
    }

    void addSumToIndexXInner(DataArr dataArr) {
        DecimalFormat df2 = new DecimalFormat("#.##");
        for (int i = 1; i < dataArr.size(); i++) {
            dataArr.get(i).sumToIndex += dataArr.get(i - 1).sumToIndex;
            dataArr.get(i).sumToIndex = (Double.parseDouble(df2.format(dataArr.get(i).sumToIndex)));
        }
    }

    void updateMinMaxY(double y){
        if(this.maxY < y)
            this.maxY = y;
        else if (this.minY > y)
            this.minY = y;
    }

    Node getRelevantSubTree (Pair<Double, Double> yRange){
        return root.getNodeInRange(yRange);
    }
}
