package de.julianpadawan.common.customFX;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.text.Text;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeText extends Text {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private ObjectProperty<LocalDateTime> value = new SimpleObjectProperty<>(this, "value") {
        @Override
        protected void invalidated() {
            if (value.getValue() == null) setText("--:--");
            else setText(FORMATTER.format(value.getValue()));
        }
    };

    public TimeText() {
        super();
        this.value.setValue(LocalDateTime.MIN);
    }

    public ObjectProperty<LocalDateTime> valueProperty() {
        return value;
    }

    public LocalDateTime getValue() {
        return value.get();
    }

    public void setValue(LocalDateTime value) {
        this.value.set(value);
    }
}
