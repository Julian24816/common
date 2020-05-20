package de.julianpadawan.common.customFX;

import de.ijf.contactDB.view.App;
import de.julianpadawan.common.db.ModelObject;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.util.concurrent.CountDownLatch;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class ObjectDialog<T extends ModelObject<?>> extends Dialog<T> {
    private static BiFunction<String, ModelObject<?>, Lock> getLock;

    protected final T editedObject;

    protected final GridPane2C gridPane2C = new GridPane2C(10);
    private final BooleanProperty saveAllowed = new SimpleBooleanProperty(this, "saveAllowed", true);
    private final Text message = new Text();
    private final Consumer<T> refreshBeforeEdit;

    private final Button okButton;
    private BooleanExpression okButtonEnabled;

    private Lock lock;

    protected ObjectDialog(String name, T editedObject, Consumer<T> refreshBeforeEdit) {
        super();
        if (editedObject != null && getLock != null && refreshBeforeEdit == null)
            throw new IllegalArgumentException("refreshBeforeEdit required when using locks");
        this.refreshBeforeEdit = refreshBeforeEdit;
        this.editedObject = editedObject;
        acquireLock(name);

        setTitle(name);
        setHeaderText((editedObject == null ? "New " : "Edit ") + name);

        TextField id = gridPane2C.addRow("ID", new TextField());
        id.setText(editedObject == null ? "<new>" : String.valueOf(editedObject.getId()));
        id.setDisable(true);
        getDialogPane().setContent(gridPane2C);

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButtonEnabled = BooleanExpression.booleanExpression(saveAllowed);
        setResultConverter(this::convertResult);
    }

    private void acquireLock(String name) {
        if (editedObject == null) return;
        if (getLock == null && refreshBeforeEdit == null) return;
        gridPane2C.addRow("Status", message);
        saveAllowed.setValue(false);
        if (getLock == null) {
            message.setText("loading current values from database...");
            App.executeLongTask(() -> refreshReloadAndAllowSave("ok"));
        } else {
            message.setText("acquiring lock...");
            App.executeLongTask(() -> {
                lock = getLock.apply(name, editedObject);
                if (lock.holdByUs()) {
                    Platform.runLater(() -> message.setText("lock acquired. loading current values from database..."));
                    refreshReloadAndAllowSave("ok. record locked by you");
                } else Platform.runLater(() -> message.setText("record locked by " + lock.getHolder()));
            });
        }
    }

    private void refreshReloadAndAllowSave(String s) {
        refreshBeforeEdit.accept(editedObject);
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> reloadValuesFromEditedObject(latch));
        try {
            latch.await();
        } catch (InterruptedException e) {
            ErrorAlert.show("refreshing object values", e);
        }
        Platform.runLater(() -> {
            saveAllowed.setValue(true);
            message.setText(s);
        });
    }

    private T convertResult(ButtonType buttonType) {
        if (lock != null && lock.holdByUs()) lock.release();
        if (!buttonType.equals(ButtonType.OK)) return null;
        else if (editedObject == null) return createNew();
        return save() ? editedObject : null;
    }

    protected abstract void reloadValuesFromEditedObject(final CountDownLatch latch);

    protected abstract T createNew();

    protected abstract boolean save();

    private static void useLocks(BiFunction<String, ModelObject<?>, Lock> getLock) {
        ObjectDialog.getLock = getLock;
    }

    protected TextField addTextField(final String label, String prompt, Function<T, String> getter,
                                     boolean requiredNotBlank) {
        return addTextField(label, prompt, getter, requiredNotBlank, -1);
    }

    protected TextField addTextField(final String label, String prompt, Function<T, String> getter,
                                     boolean requiredNotBlank, final int maxLength) {
        final TextField textField = gridPane2C.addRow(label, new TextField());
        textField.setPromptText(prompt);
        if (editedObject != null) textField.setText(getter.apply(editedObject));
        if (requiredNotBlank) addOKRequirement(CustomBindings.matches(textField, "\\s*").not());
        if (maxLength >= 0) Util.cutoff(textField.textProperty(), maxLength);
        return textField;
    }

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
