public class Node {
    double y;
    DataArr xList;
    Node left, right;

    Node(DataFormat df){
        this.y = df.y;
        this.xList = new DataArr(df);
        this.left = null;
        this.right = null;
    }

    boolean isLeaf(){
        return left == null && right == null;
    }
}
