package de.julianpadawan.common.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class ModelTableDefinition<T extends ModelObject<T>> {
    final String tableName;

    final int numberOfColumns;
    final List<ColumnType<?>> types;
    final List<Function<T, ?>> getters;

    final String columnNames;
    final String insertPlaceholders;
    final String updatePlaceholders;

    public ModelTableDefinition(String tableName) {
        this.tableName = tableName;

        this.numberOfColumns = 0;
        this.types = new ArrayList<>();
        this.getters = new ArrayList<>();

        this.columnNames = "";
        this.insertPlaceholders = "";
        this.updatePlaceholders = "";
    }

    private <C> ModelTableDefinition(ModelTableDefinition<T> extend, String anotherColumn, ColumnType<C> type, Function<T, C> getter) {
        this.tableName = extend.tableName;

        this.numberOfColumns = extend.numberOfColumns + 1;
        this.types = new ArrayList<>(extend.types);
        this.types.add(type);
        this.getters = new ArrayList<>(extend.getters);
        this.getters.add(getter);

        if (this.numberOfColumns == 1) {
            columnNames = anotherColumn;
            insertPlaceholders = "?";
            updatePlaceholders = anotherColumn + "=?";
        } else {
            columnNames = extend.columnNames + "," + anotherColumn;
            insertPlaceholders = extend.insertPlaceholders + ",?";
            updatePlaceholders = extend.updatePlaceholders + "," + anotherColumn + "=?";
        }
    }

    public <C> ModelTableDefinition<T> withColumn(String anotherColumn, ColumnType<C> type, Function<T, C> getter) {
        return new ModelTableDefinition<>(this, anotherColumn, type, getter);
    }

    public String getInsertSQL() {
        return "INSERT INTO " + tableName + "(" + columnNames + ") VALUES (" + insertPlaceholders + ")";
    }

    public void setInsertParams(PreparedStatement statement, Object[] params) throws SQLException {
        if (params.length != getNumberOfColumns()) throw new IllegalArgumentException("wrong number of parameters");
        for (int i = 0; i < types.size(); i++) {
            types.get(i).apply(statement, i + 1, params[i]);
        }
    }

    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    public String getUpdateSQL() {
        return "UPDATE " + tableName + " SET " + updatePlaceholders + " WHERE id=?";
    }

    public void setUpdateParams(PreparedStatement statement, T obj) throws SQLException {
        for (int i = 0; i < types.size(); i++) {
            types.get(i).apply(statement, i + 1, getters.get(i).apply(obj));
        }
        statement.setInt(getNumberOfColumns() + 1, obj.getId());
    }

    public String getBaseSelectSQL() {
        return "SELECT id, " + columnNames + " FROM " + tableName;
    }
}
