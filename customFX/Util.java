package de.julianpadawan.common.customFX;

import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;

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
}
