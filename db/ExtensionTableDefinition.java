package de.julianpadawan.common.db;

import java.util.function.Function;

public final class ExtensionTableDefinition<B extends ModelObject<B>, T extends Extension<B>> {
    private final String tableName;
    private final String foreignKey;
    private final String foreignKeyPlaceholder;

    private final String columnNames;

    public ExtensionTableDefinition(String tableName, String foreignKeyColumn, Class<B> baseClass) {
        this.tableName = tableName;
        this.foreignKey = foreignKeyColumn;
        this.foreignKeyPlaceholder = foreignKeyColumn + "=?";

        this.columnNames = foreignKeyColumn;
    }

    protected <C> ExtensionTableDefinition(ExtensionTableDefinition<B, T> extend, String anotherColumn, ColumnType<C> type, Function<T, C> getter) {
        this.tableName = extend.tableName;
        this.foreignKey = extend.foreignKey;
        this.foreignKeyPlaceholder = extend.foreignKeyPlaceholder;

        this.columnNames = extend.columnNames + "," + anotherColumn;
    }

    public <C> ExtensionTableDefinition<B, T> withColumn(String anotherColumn, ColumnType<C> type, Function<T, C> getter) {
        return new ExtensionTableDefinition<>(this, anotherColumn, type, getter);
    }


    public String getSelectSQL() {
        return "SELECT " + columnNames + " FROM " + tableName + " WHERE " + foreignKeyPlaceholder;
    }
}
