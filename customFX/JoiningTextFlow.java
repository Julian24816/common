package de.julianpadawan.common.customFX;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class JoiningTextFlow extends TextFlow {
    @SafeVarargs
    public JoiningTextFlow(final Text mainText, final ObservableValue<String>... text) {
        super(mainText);
        for (ObservableValue<String> observableStringValue : text) {
            final Text textNode = new Text();
            textNode.textProperty().bind(new ObjectBinding<>() {
                {
                    bind(observableStringValue);
                }

                @Override
                protected String computeValue() {
                    final String string = observableStringValue.getValue();
                    if (string == null) return "";
                    return string.isEmpty() ? "" : ", " + string;
                }
            });
            getChildren().add(textNode);
        }
    }
}
