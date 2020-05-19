package de.julianpadawan.common.customFX;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

public class BooleanRadioToggle extends HBox {

    private final ReadOnlyBooleanWrapper value = new ReadOnlyBooleanWrapper(this, "value");
    private final RadioButton falseButton, trueButton;
    private final ToggleGroup toggleGroup;

    public BooleanRadioToggle(String falseText, String trueText, boolean value) {
        super(5);
        toggleGroup = new ToggleGroup();

        falseButton = new RadioButton(falseText);
        falseButton.setToggleGroup(toggleGroup);

        trueButton = new RadioButton(trueText);
        trueButton.setToggleGroup(toggleGroup);

        getChildren().addAll(falseButton, trueButton);
        this.value.bind(trueButton.selectedProperty());
        setValue(value);
    }

    public ReadOnlyBooleanProperty valueProperty() {
        return value.getReadOnlyProperty();
    }

    public boolean isValue() {
        return value.get();
    }

    public void setValue(boolean value) {
        toggleGroup.selectToggle((value ? trueButton : falseButton));
    }
}
