package de.julianpadawan.common.customFX;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DateTimePicker extends HBox {
    private final DatePicker date = new DatePicker();
    private final TimeTextField time = new TimeTextField();

    private final ReadOnlyObjectWrapper<LocalDateTime> value = new ReadOnlyObjectWrapper<>(this, "value");

    public DateTimePicker(LocalDateTime value) {
        this();
        setValue(value);
    }

    public DateTimePicker() {
        super(10);
        getChildren().addAll(date, time);
        Util.applyAfterFocusLost(date);
        HBox.setHgrow(time, Priority.ALWAYS);
        date.valueProperty().addListener(this::invalidated);
        time.valueProperty().addListener(this::invalidated);
    }

    public DateTimePicker(LocalDate date, LocalTime time) {
        this();
        setDate(date);
        setTime(time);
    }

    private void invalidated(Observable observable) {
        if (date.getValue() == null || time.getValue() == null) value.set(null);
        else value.set(LocalDateTime.of(date.getValue(), time.getValue()));
    }

    public ReadOnlyObjectProperty<LocalDateTime> valueProperty() {
        return value.getReadOnlyProperty();
    }

    public LocalDateTime getValue() {
        return value.get();
    }

    public void setDate(LocalDate value) {
        date.setValue(value);
    }

    public void setTime(LocalTime value) {
        time.setValue(value);
    }

    public void setValue(LocalDateTime value) {
        if (getValue() == null && value == null) return;
        date.setValue(value == null ? null : value.toLocalDate());
        time.setValue(value == null ? null : value.toLocalTime());
    }
}
