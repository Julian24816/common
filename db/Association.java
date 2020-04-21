package de.julianpadawan.common.db;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.util.Objects;

public abstract class Association<A extends ModelObject<A>, B extends ModelObject<B>> implements DatabaseObject {
    private final ReadOnlyObjectWrapper<A> first = new ReadOnlyObjectWrapper<>(this, "first");
    private final ReadOnlyObjectWrapper<B> second = new ReadOnlyObjectWrapper<>(this, "second");

    protected Association(A first, B second) {
        this.first.setValue(Objects.requireNonNull(first));
        this.second.setValue(Objects.requireNonNull(second));
    }

    public ReadOnlyObjectProperty<A> firstProperty() {
        return first.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<B> secondProperty() {
        return second.getReadOnlyProperty();
    }

    public A getFirst() {
        return first.get();
    }

    public B getSecond() {
        return second.get();
    }
}
