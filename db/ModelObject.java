package de.julianpadawan.common.db;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableStringValue;

import java.util.Objects;

public abstract class ModelObject<T extends ModelObject<?>> implements Comparable<T>, DatabaseObject {
    private final int id;

    protected ModelObject(int id) {
        this.id = id;
    }

    @Override
    public abstract String toString();

    @Override
    public boolean equals(Object o) {
        //noinspection ObjectComparison
        if (this == o) return true;
        if (o == null || !getClass().equals(o.getClass())) return false;
        ModelObject<?> that = (ModelObject<?>) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public final int getId() {
        return id;
    }

    public final ReadOnlyIntegerProperty idProperty() {
        return new SimpleIntegerProperty(this, "id", id);
    }

    @Override
    public int compareTo(T o) {
        return getDisplayName().compareTo(o.getDisplayName());
    }

    public final String getDisplayName() {
        return displayNameProperty().get();
    }

    public abstract ObservableStringValue displayNameProperty();
}
