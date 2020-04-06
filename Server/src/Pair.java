/* Class for storing two values together */
public class Pair<T, K> {
    private T p1;
    private K p2;

    Pair(T p1, K p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public Pair() {
    }

    public T getP1() {
        return p1;
    }

    public K getP2() {
        return p2;
    }

    public void setP1(T p1) { this.p1 = p1; }

    public void setP2(K p2) { this.p2 = p2; }

    @Override
    public String toString() {
        return "Pair{" +
                "p1=" + p1 +
                ", p2=" + p2 +
                '}';
    }

    public void swap() {
        T temp = p1;
        p1 = (T) p2;
        p2 = (K) temp;
    }
}
