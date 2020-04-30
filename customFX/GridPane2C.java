package de.julianpadawan.common.customFX;

import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;

public class GridPane2C extends GridPane {
    private final double space;
    private int row;

    public GridPane2C(double space) {
        super();
        this.space = space;
        setHgap(space);
        setVgap(space);
    }

    public <T extends Node> T addRow(String label, T node) {
        final Text labelNode = new Text(label);
        addRow(row++, labelNode, node);
        GridPane.setHgrow(node, Priority.ALWAYS);
        GridPane.setValignment(labelNode, VPos.BASELINE);
        return node;
    }

    public void addSeparator() {
        add(new Separator(), 0, row++, 2, 1);
    }

    public void addButtonRow(Button... buttons) {
        add(new FlowPane(space, space, buttons), 1, row++);
    }

    public void skipRow() {
        row++;
    }

    public <T extends Node> T addWideRow(String label, T node) {
        final Label labelNode = new Label(label);
        labelNode.setLabelFor(node);
        add(labelNode, 0, row++);
        add(node, 0, row++, 2, 1);
        GridPane.setHgrow(node, Priority.ALWAYS);
        return node;
    }
}
