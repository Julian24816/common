package de.julianpadawan.common.customFX;

import de.julianpadawan.common.db.ModelObject;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CreatingChoiceBox<T extends ModelObject<T>> extends HBox {

    private final ReadOnlyObjectWrapper<T> valueProperty = new ReadOnlyObjectWrapper<>();
    private final ObservableList<T> choices = FXCollections.observableArrayList();

    private final ChoiceBox<Entry<T>> choiceBox;
    private final Supplier<Dialog<T>> newDialog;
    private final Function<T, Dialog<T>> editDialog;
    private final Consumer<T> onNew;

    private CreatingChoiceBox(Collection<T> choices, Supplier<Dialog<T>> newDialog, Consumer<T> onNew, Function<T, Dialog<T>> editDialog, boolean allowSelectNull) {
        super(10);
        this.newDialog = newDialog;
        this.onNew = onNew == null ? this::setValue : onNew;
        this.editDialog = editDialog;

        choiceBox = new ChoiceBox<>();
        getChildren().add(choiceBox);
        HBox.setHgrow(choiceBox, Priority.ALWAYS);
        choiceBox.setMaxWidth(Double.MAX_VALUE);
        choiceBox.setConverter(Entry.getStringConverter());
        choiceBox.getSelectionModel().selectedItemProperty().addListener(this::selectionChanged);
        if (newDialog != null) choiceBox.getItems().add(Entry.placeholder());
        if (editDialog != null) choiceBox.setOnMouseClicked(this::doubleClick);

        this.choices.addListener(this::onListChanged);
        this.choices.addAll(choices);

        if (editDialog != null) addButton("Edit", this::showEditDialog);
        if (allowSelectNull) addButton("Remove", () -> choiceBox.getSelectionModel().select(null));
    }

    private void selectionChanged(ObservableValue<?> observableValue, Entry<T> oldValue, Entry<T> newValue) {
        if (newValue == null) valueProperty.set(null);
        else if (newValue.isPlaceholder()) {
            assert newDialog != null;
            final Optional<T> optionalItem = Optional.ofNullable(newDialog.get()).flatMap(Dialog::showAndWait);
            optionalItem.ifPresentOrElse(onNew, () -> choiceBox.getSelectionModel().select(oldValue));
        } else {
            valueProperty.set(newValue.get());
        }
    }

    private void doubleClick(MouseEvent event) {
        if (!event.getButton().equals(MouseButton.PRIMARY) || event.getClickCount() != 2 || choiceBox.getValue() == null)
            return;
        showEditDialog();
    }

    private void onListChanged(ListChangeListener.Change<? extends T> c) {
        while (c.next()) {
            if (c.wasPermutated()) {
                ErrorAlert.show("ListChange", new UnsupportedOperationException("entries list must not be permutated"));
            } else if (c.wasUpdated()) {
                ErrorAlert.show("ListChange", new UnsupportedOperationException("entries list must not be updated"));
            } else {
                for (T added : c.getAddedSubList()) choiceBox.getItems().add(Entry.of(added));
                for (T removed : c.getRemoved()) choiceBox.getItems().remove(Entry.of(removed));
                FXCollections.sort(choiceBox.getItems());
            }
        }
    }

    private void addButton(String text, Runnable onAction) {
        Button selectNullButton = new Button(text);
        selectNullButton.setOnAction(event -> onAction.run());
        bindSelectedToEnabled(selectNullButton);
        getChildren().add(selectNullButton);
    }

    private void showEditDialog() {
        editDialog.apply(choiceBox.getValue().content).showAndWait().ifPresent(edited ->
                FXCollections.sort(choiceBox.getItems())
        );
    }

    private void bindSelectedToEnabled(Button button) {
        button.setDisable(true);
        choiceBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> button.setDisable(newValue == null));
    }

    public static <T extends ModelObject<T>> CreatingChoiceBox<T> simple(Collection<T> choices) {
        return new CreatingChoiceBox<>(choices, null, null, null, false);
    }

    public static <T extends ModelObject<T>> CreatingChoiceBox<T> simple(Collection<T> choices, Supplier<Dialog<T>> newDialog, Function<T, Dialog<T>> editDialog) {
        return new CreatingChoiceBox<>(choices, newDialog, null, editDialog, false);
    }

    public static <T extends ModelObject<T>> CreatingChoiceBox<T> nullable(Collection<T> choices, Supplier<Dialog<T>> newDialog, Function<T, Dialog<T>> editDialog) {
        return new CreatingChoiceBox<>(choices, newDialog, null, editDialog, true);
    }

    public static <T extends ModelObject<T>> CreatingChoiceBox<T> creating(Collection<T> choices, Supplier<Dialog<T>> newDialog, Consumer<T> onNew) {
        return new CreatingChoiceBox<>(choices, newDialog, onNew, null, false);
    }

    public ReadOnlyObjectProperty<T> valueProperty() {
        return valueProperty.getReadOnlyProperty();
    }

    public T getValue() {
        return valueProperty.get();
    }

    public void setValue(T value) {
        if (value == null) choiceBox.getSelectionModel().select(null);
        else {
            if (!choices.contains(value)) choices.add(value);
            choiceBox.getSelectionModel().select(Entry.of(value));
        }
    }

    public ObservableList<T> getChoices() {
        return choices;
    }

    public static final class Entry<T extends ModelObject<T>> implements Comparable<Entry<T>> {
        private final T content;

        private Entry(T content) {
            this.content = content;
        }

        public static <T extends ModelObject<T>> StringConverter<Entry<T>> getStringConverter() {
            return new StringConverter<>() {
                @Override
                public String toString(Entry<T> entry) {
                    return entry.isPlaceholder() ? "new..." : entry.get().getDisplayName();
                }

                @Override
                public Entry<T> fromString(String s) {
                    return null;
                }
            };
        }

        public boolean isPlaceholder() {
            return content == null;
        }

        public T get() {
            return content;
        }

        public static <T extends ModelObject<T>> Entry<T> placeholder() {
            return new Entry<>(null);
        }

        public static <T extends ModelObject<T>> Entry<T> of(T obj) {
            return new Entry<>(Objects.requireNonNull(obj));
        }

        @Override
        public String toString() {
            return isPlaceholder() ? "PlaceholderEntry" : "Entry{content=" + content + "}";
        }

        @Override
        public int compareTo(Entry<T> o) {
            if (content == null && o.content == null) return 0;
            if (content == null) return 1;
            if (o.content == null) return -1;
            return content.compareTo(o.content);
        }

        @Override
        public boolean equals(Object o) {
            //noinspection ObjectComparison
            if (this == o) return true;
            if (!(o instanceof Entry)) return false;
            Entry<?> entry = (Entry<?>) o;
            return Objects.equals(content, entry.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(content);
        }
    }
}
