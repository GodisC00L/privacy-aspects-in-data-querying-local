/* Generic node class */

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Node<T> {
    private double key;
    //private List<T> valueList;
    private Map<Double, List<T>> map;
    private Node<T> right;
    private Node<T> left;

    Node(double key, double timestamp, T values) {
        this.key = key;
        this.map = new LinkedHashMap<>();
        put(map, timestamp, values);
        this.right = null;
        this.left = null;
    }

    double getKey() {
        return key;
    }

    Node<T> getRight() {
        return right;
    }

    Node<T> getLeft() {
        return left;
    }

    void addValue(double timestamp, T values) {
        put(this.map, timestamp, values);
    }

    public Map<Double, List<T>> getMap() {
        return map;
    }

    void getValuesForTimestamp(double timestamp, List<Double> list) {
        //finds a velocity for timestamp
        if (getMap().get(timestamp) != null) {
            List<Pair<Double,Double>> localList = (List<Pair<Double, Double>>) getMap().get(timestamp);
            for (Pair<Double, Double> p : localList) {
                if (p.getP1() == timestamp)
                    list.add(p.getP2());
            }
        }
    }

    void setRight(Node<T> right) {
        this.right = right;
    }

    void setLeft(Node<T> left) {
        this.left = left;
    }

    private void put(Map<Double, List<T>> map, double timestamp, T values) {
        if(map.get(timestamp) == null) {
            List<T> list = new ArrayList<>();
            list.add(values);
            map.put(timestamp, list);
        } else {
            map.get(timestamp).add(values);
        }
    }

    boolean isLeaf() {
        return (this.left == null && this.right == null);
    }

    void getValuesForSubtree(double timestamp, List<Double> list) {
        if(getLeft() != null)
            getLeft().getValuesForSubtree(timestamp, list);
        if(getRight() != null)
            getRight().getValuesForSubtree(timestamp, list);
        getValuesForTimestamp(timestamp, list);
    }
}
