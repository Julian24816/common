package de.julianpadawan.common.customFX;

import de.julianpadawan.common.db.ModelObject;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;

public abstract class ObjectDialog<T extends ModelObject<?>> extends Dialog<T> {

    protected final T editedObject;
    protected final GridPane2C gridPane2C;

    private final Button okButton;
    private BooleanExpression okButtonEnabled;

    protected ObjectDialog(String name, T editedObject) {
        super();
        this.editedObject = editedObject;
        setTitle(name);
        setHeaderText((editedObject == null ? "New " : "Edit ") + name);

        gridPane2C = new GridPane2C(10);
        final TextField id = gridPane2C.addRow("id", new TextField());
        id.setText(editedObject == null ? "<new>" : String.valueOf(editedObject.getId()));
        id.setDisable(true);
        getDialogPane().setContent(gridPane2C);

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButtonEnabled = BooleanExpression.booleanExpression(new SimpleBooleanProperty(true));
        setResultConverter(this::convertResult);
    }

    private T convertResult(ButtonType buttonType) {
        if (!buttonType.equals(ButtonType.OK)) return null;
        else if (editedObject == null) return createNew();
        else return save() ? editedObject : null;
    }

    protected abstract T createNew();

    protected abstract boolean save();

    protected void addOKRequirement(ObservableValue<Boolean> value) {
        okButtonEnabled.removeListener(this::onEnableInvalidated);
        okButtonEnabled = okButtonEnabled.and(BooleanExpression.booleanExpression(value));
        okButtonEnabled.addListener(this::onEnableInvalidated);
        onEnableInvalidated(null);
    }

    private void onEnableInvalidated(Observable observable) {
        okButton.setDisable(!okButtonEnabled.getValue());
    }
}
