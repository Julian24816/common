package de.julianpadawan.common.customFX;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TextField;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeTextField extends TextField {
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter LAX_TIME_FORMATTER = DateTimeFormatter.ofPattern("H:m");
    private final ReadOnlyObjectWrapper<LocalTime> value;

    public TimeTextField() {
        this(null);
    }

    public TimeTextField(LocalTime value) {
        super();
        this.value = new ReadOnlyObjectWrapper<>(this, "time", value);
        if (value != null) setText(TIME_FORMATTER.format(value));

        setPromptText("hh:mm");
        textProperty().addListener(observable -> {
            try {
                final LocalTime time = LocalTime.from(LAX_TIME_FORMATTER.parse(getText()));
                this.value.setValue(time);
            } catch (DateTimeParseException e) {
                this.value.set(null);
            }
        });
    }

    public ReadOnlyObjectProperty<LocalTime> valueProperty() {
        return value.getReadOnlyProperty();
    }

    public LocalTime getValue() {
        return value.getValue();
    }

    public void setValue(LocalTime value) {
        if (value != null) setText(TIME_FORMATTER.format(value));
        else setText("");
    }
}
