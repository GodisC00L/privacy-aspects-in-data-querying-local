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

    Node getNodeInRange(Pair<Double, Double> yRange) {
        if(yRange.getP1() <= y && y <= yRange.getP2())
            return this;
        if (yRange.getP1() > y)
            return this.right.getNodeInRange(yRange);
        if (yRange.getP2() < y)
            return this.left.getNodeInRange(yRange);
        return null;
    }
}
