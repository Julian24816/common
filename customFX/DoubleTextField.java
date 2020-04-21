package de.julianpadawan.common.customFX;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.scene.control.TextField;

public class DoubleTextField extends TextField {
    private final ReadOnlyBooleanWrapper valid = new ReadOnlyBooleanWrapper(this, "valid");
    private final ReadOnlyDoubleWrapper value = new ReadOnlyDoubleWrapper(this, "value");

    public DoubleTextField(double value, boolean allowNegative) {
        super();
        textProperty().addListener((observable, oldText, newText) -> {
            if (newText.contains(",")) {
                setText(newText.replace(',', '.'));
                return;
            }
            try {
                final double newValue = Double.parseDouble(newText);
                if (newValue >= 0 || allowNegative) {
                    this.value.set(newValue);
                    valid.set(true);
                } else valid.set(false);
            } catch (NumberFormatException e) {
                valid.set(false);
            }
        });
        setValue(value);
    }

    public ReadOnlyBooleanProperty validProperty() {
        return valid.getReadOnlyProperty();
    }

    public boolean isValid() {
        return valid.get();
    }

    public ReadOnlyDoubleProperty valueProperty() {
        return value.getReadOnlyProperty();
    }

    public double getValue() {
        return value.get();
    }

    public void setValue(double value) {
        setText(Double.toString(value));
    }
}
