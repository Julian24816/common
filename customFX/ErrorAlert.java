package de.julianpadawan.common.customFX;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorAlert extends Alert {
    public ErrorAlert(String context, Throwable exception) {
        super(AlertType.ERROR, String.format("Context: %s\nMessage: %s", context, exception.getMessage()));
        setTitle("Error");
        setHeaderText("An " + exception.getClass() + " occurred.");

        final StringWriter errorMessage = new StringWriter();
        exception.printStackTrace(new PrintWriter(errorMessage));
        final TextArea textArea = new TextArea(errorMessage.toString());
        textArea.setEditable(false);
        getDialogPane().setExpandableContent(textArea);
    }

    public static void show(String context, Throwable e) {
        Platform.runLater(() -> new ErrorAlert(context, e).show());
    }
}
