package de.julianpadawan.common.db;

import java.util.Objects;

public abstract class Extension<T extends ModelObject<T>> implements DatabaseObject {
    private final T base;

    protected Extension(T base) {
        this.base = Objects.requireNonNull(base);
    }

    public T getBase() {
        return base;
    }

    public int getID() {
        return base.getId();
    }

    @Override
    public abstract String toString();

    @Override
    public boolean equals(Object o) {
        //noinspection ObjectComparison
        if (this == o) return true;
        if (o == null || !getClass().equals(o.getClass())) return false;
        Extension<?> extension = (Extension<?>) o;
        return base.equals(extension.base);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base);
    }
}
