package de.julianpadawan.common.customFX;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TextField;

import java.time.Duration;

public class DurationMinutesTextField extends TextField {
    private final ReadOnlyObjectWrapper<Duration> value = new ReadOnlyObjectWrapper<>(this, "duration");

    public DurationMinutesTextField() {
        this(null);
    }

    public DurationMinutesTextField(Duration value) {
        textProperty().addListener(this::invalidated);
        setValue(value);
    }

    private void invalidated(Observable observable) {
        try {
            final String text = getText();
            final long minutes = Long.parseLong(text);
            this.value.set(Duration.ofMinutes(minutes));
        } catch (NumberFormatException e) {
            this.value.set(null);
        }
    }

    public ReadOnlyObjectProperty<Duration> valueProperty() {
        return value.getReadOnlyProperty();
    }

    public Duration getValue() {
        return value.get();
    }

    public void setValue(Duration value) {
        setText(value == null ? "" : Long.toString(value.toMinutes()));
    }
}
