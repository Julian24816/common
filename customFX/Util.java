package de.julianpadawan.common.customFX;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class Util {
    private Util() {
    }

    public static void applyAfterFocusLost(final DatePicker datePicker) {
        datePicker.focusedProperty().addListener((observable, oldValue, focused) -> {
            if (focused) return;
            try {
                datePicker.setValue(datePicker.getConverter().fromString(datePicker.getEditor().getText()));
            } catch (DateTimeParseException e) {
                datePicker.getEditor().setText(datePicker.getConverter().toString(datePicker.getValue()));
            }
        });
        datePicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                final LocalDate localDate = datePicker.getConverter().fromString(newValue);
                if (datePicker.getConverter().toString(localDate).equals(newValue))
                    datePicker.setValue(localDate);
            } catch (DateTimeParseException ignored) {
            }
        });
    }

    public static Button button(String label, String color, Runnable onAction) {
        final Button button = button(label, onAction);
        button.setStyle("-fx-base: " + color);
        return button;
    }

    public static Button button(String label, Runnable onAction) {
        final Button button = new Button(label);
        button.setOnAction(event -> {
            onAction.run();
            event.consume();
        });
        return button;
    }

    public static Button button(ObservableValue<String> label, Runnable onAction) {
        final Button button = button(label.getValue(), onAction);
        button.textProperty().bind(label);
        return button;
    }

    public static Region hBoxSpacer() {
        final Region spacer = new Region();
        spacer.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }
}
