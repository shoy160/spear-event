package cn.spear.event.core.lang;

/**
 * @author luoyong
 * @date 2021/8/30
 */
public class Tuple<A, B> {
    private static final Tuple EMPTY = new Tuple();
    private A first;
    private B second;

    private Tuple() {
    }

    public static <A, B> Tuple<A, B> empty() {
        return EMPTY;
    }

    public static <A, B> Tuple<A, B> of(A first, B second) {
        Tuple<A, B> tuple = new Tuple();
        tuple.setA(first);
        tuple.setB(second);
        return tuple;
    }

    public A getA() {
        return this.first;
    }

    public void setA(A first) {
        this.first = first;
    }

    public B getB() {
        return this.second;
    }

    public void setB(B second) {
        this.second = second;
    }
}
