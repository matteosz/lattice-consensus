package cs451.helper;

import java.util.Objects;

public class GenericPair<T, U> {
    private T t;
    private U u;

    public GenericPair (T t, U u) {
        this.t = t;
        this.u = u;
    }

    public T getT() {
        return t;
    }

    public U getU() {
        return u;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericPair<?, ?> that = (GenericPair<?, ?>) o;
        return t.equals(that.t) && u.equals(that.u);
    }

    @Override
    public int hashCode() {
        return Objects.hash(t, u);
    }
}
