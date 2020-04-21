package de.julianpadawan.common.customFX;

import de.julianpadawan.common.db.Association;
import de.julianpadawan.common.db.AssociationFactory;
import de.julianpadawan.common.db.ModelObject;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AssociationFlowPane<A extends ModelObject<A>, B extends ModelObject<B>, T extends Association<A, B>> extends FlowPane {

    private final AssociationFactory<A, B, T> factory;
    private final A first;
    private final Supplier<Dialog<B>> newDialog;
    private final Function<B, Dialog<B>> editDialog;

    private final CreatingChoiceBox<B> bChoiceBox;

    private final ObservableList<T> items = FXCollections.observableArrayList();
    private final ObservableList<PendingAssociation<A, B>> pendingItems = FXCollections.observableArrayList();

    public AssociationFlowPane(AssociationFactory<A, B, T> factory, A first, Collection<B> choices, Supplier<Dialog<B>> newDialog, Function<B, Dialog<B>> editDialog) {
        super(Orientation.HORIZONTAL, 10, 10);
        this.factory = factory;
        this.first = first;
        this.newDialog = newDialog;
        this.editDialog = editDialog;

        bChoiceBox = CreatingChoiceBox.creating(choices, newDialog, this::addAssociation);
        bChoiceBox.valueProperty().addListener(this::onChoiceBoxChanged);
        getChildren().add(bChoiceBox);

        items.addListener(this::onListChanged);
        pendingItems.addListener(this::onPendingListChanged);
        if (first != null) items.addAll(factory.getAll(first));
    }

    private void addAssociation(B second) {
        bChoiceBox.setValue(null);
        if (first == null) pendingItems.add(new PendingAssociation<>(second));
        else {
            final T association = factory.create(first, second);
            if (association != null) items.add(association);
        }
    }

    private void onChoiceBoxChanged(Observable observable) {
        if (bChoiceBox.getValue() == null) return;
        addAssociation(bChoiceBox.getValue());
    }

    private void onListChanged(ListChangeListener.Change<? extends T> c) {
        while (c.next()) {
            if (c.wasPermutated()) {
                ErrorAlert.show("ListChange", new UnsupportedOperationException("entries list must not be permutated"));
            } else if (c.wasUpdated()) {
                ErrorAlert.show("ListChange", new UnsupportedOperationException("entries list must not be updated"));
            } else {
                for (T added : c.getAddedSubList())
                    addAssociationItem(added.getSecond(), b -> {
                        if (factory.delete(added)) items.remove(added);
                    });
                for (T removed : c.getRemoved()) removeAssociationItem(removed.getSecond());
            }
        }
    }

    private void onPendingListChanged(ListChangeListener.Change<? extends PendingAssociation<A, B>> c) {
        while (c.next()) {
            if (c.wasPermutated()) {
                ErrorAlert.show("ListChange", new UnsupportedOperationException("entries list must not be permutated"));
            } else if (c.wasUpdated()) {
                ErrorAlert.show("ListChange", new UnsupportedOperationException("entries list must not be updated"));
            } else {
                for (PendingAssociation<A, B> added : c.getAddedSubList())
                    addAssociationItem(added.with, b -> pendingItems.remove(added));
                for (PendingAssociation<A, B> removed : c.getRemoved()) removeAssociationItem(removed.with);
            }
        }
    }

    private void addAssociationItem(final B second, final Consumer<B> onRemove) {
        bChoiceBox.getChoices().remove(second);
        getChildren().add(getChildren().size() - 1, new AssociationItem<>(second, editDialog, onRemove));
    }

    private void removeAssociationItem(final B second) {
        bChoiceBox.getChoices().add(second);
        getChildren().remove(new AssociationItem<>(second, null, null));
    }

    public Collection<T> associateAll(A first) {
        if (this.first != null) throw new IllegalStateException();
        List<T> list = new LinkedList<>();
        pendingItems.forEach(pending -> Optional.ofNullable(factory.create(first, pending.with)).ifPresent(list::add));
        pendingItems.clear();
        return list;
    }

    private static final class PendingAssociation<A extends ModelObject<A>, B extends ModelObject<B>> {
        private final B with;

        private PendingAssociation(B with) {
            this.with = Objects.requireNonNull(with);
        }

        @Override
        public boolean equals(Object o) {
            //noinspection ObjectComparison
            if (this == o) return true;
            if (o == null || !getClass().equals(o.getClass())) return false;
            PendingAssociation<?, ?> that = (PendingAssociation<?, ?>) o;
            return with.equals(that.with);
        }

        @Override
        public int hashCode() {
            return Objects.hash(with);
        }
    }

    private static final class AssociationItem<T extends ModelObject<T>> extends HBox {
        private final T item;

        private AssociationItem(T item, Function<T, Dialog<T>> editDialog, Consumer<T> onRemove) {
            super();
            this.item = item;
            Button editButton = new Button();
            editButton.textProperty().bind(item.displayNameProperty());
            editButton.setOnAction(event -> editDialog.apply(item).show());
            Button removeButton = new Button("X");
            removeButton.setOnAction(event -> onRemove.accept(item));
            getChildren().addAll(editButton, removeButton);
            //TODO sorting?
        }

        @Override
        public boolean equals(Object o) {
            //noinspection ObjectComparison
            if (this == o) return true;
            if (o == null || !getClass().equals(o.getClass())) return false;
            AssociationItem<?> that = (AssociationItem<?>) o;
            return item.equals(that.item);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item);
        }
    }
}
