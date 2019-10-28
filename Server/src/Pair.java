/* Class for storing two values together */
public class Pair<T, K> {
    private T p1;
    private K p2;

    Pair(T p1, K p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public T getP1() {
        return p1;
    }

    public K getP2() {
        return p2;
    }
}
