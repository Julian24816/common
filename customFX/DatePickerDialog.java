package de.julianpadawan.common.customFX;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DatePickerDialog extends Dialog<LocalDate> {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @SuppressWarnings("FieldCanBeLocal")
    // this is no local variable so that it will not be removed from the heap by the garbage collector, when it is still needed
    private final ObservableValue<Boolean> okEnabled;

    private DatePickerDialog(String label, LocalDate after, LocalDate before) {
        super();
        setTitle("DatePicker");
        setHeaderText("Pick a Date" + (label != null ? ": " + label : ""));

        final DatePicker datePicker = new DatePicker();
        Util.applyAfterFocusLost(datePicker);
        datePicker.setMaxWidth(Double.MAX_VALUE);
        getDialogPane().setContent(new BorderPane(datePicker, getLabel(after, before), null, null, null));

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        final Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);

        BooleanExpression enabled = datePicker.valueProperty().isNotNull();
        if (after != null)
            enabled = enabled.and(CustomBindings.applyToBoolean(datePicker.valueProperty(), LocalDate::isAfter, after));
        if (before != null)
            enabled = enabled.and(CustomBindings.applyToBoolean(datePicker.valueProperty(), LocalDate::isBefore, before));
        okEnabled = enabled;
        okEnabled.addListener((observable, oldValue, newValue) -> okButton.setDisable(!newValue));
        okButton.setDisable(!okEnabled.getValue());

        setResultConverter(buttonType -> ButtonType.OK.equals(buttonType) ? datePicker.getValue() : null);
    }

    private Label getLabel(LocalDate after, LocalDate before) {
        if (after == null && before == null) return null;
        final Label label = new Label((after != null ? "after: " + DATE_FORMAT.format(after) : "")
                + (after != null && before != null ? ", " : "")
                + (before != null ? "before: " + DATE_FORMAT.format(before) : "")
        );
        label.setPadding(new Insets(0, 6, 0, 6));
        return label;
    }

    public static DatePickerDialog any() {
        return new DatePickerDialog(null, null, null);
    }

    public static DatePickerDialog any(String label) {
        return new DatePickerDialog(label, null, null);
    }

    public static DatePickerDialog after(LocalDate after) {
        return new DatePickerDialog(null, after, null);
    }

    public static DatePickerDialog after(String label, LocalDate after) {
        return new DatePickerDialog(label, after, null);
    }

    public static DatePickerDialog before(LocalDate before) {
        return new DatePickerDialog(null, null, before);
    }

    public static DatePickerDialog before(String label, LocalDate before) {
        return new DatePickerDialog(label, null, before);
    }

    public static DatePickerDialog between(LocalDate before, LocalDate after) {
        return new DatePickerDialog(null, before, after);
    }

    public static DatePickerDialog between(String label, LocalDate before, LocalDate after) {
        return new DatePickerDialog(label, before, after);
    }
}
