package de.julianpadawan.common.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class SpecialisationTableDefinition<B extends ModelObject<B>, T extends B, Proof extends Specialisation<B, T>> {
    private final ModelTableDefinition<B> base;
    private final String tableName;
    private final String foreignKeyColumn;
    private final String join;

    private final List<ColumnType<?>> moreTypes;
    private final List<Function<T, ?>> moreGetters;

    private final String moreColumnNames;
    private final String moreInsertPlaceholders;
    private final String moreUpdatePlaceholders;

    public SpecialisationTableDefinition(ModelTableDefinition<B> base, String tableName, String foreignKeyColumn) {
        this.base = base;
        this.tableName = tableName;
        this.foreignKeyColumn = foreignKeyColumn;
        this.join = base.tableName + " JOIN " + tableName + " ON ID=" + foreignKeyColumn;

        this.moreTypes = new ArrayList<>();
        this.moreGetters = new ArrayList<>();

        this.moreColumnNames = "";
        this.moreInsertPlaceholders = "";
        this.moreUpdatePlaceholders = "";
    }

    private <C> SpecialisationTableDefinition(SpecialisationTableDefinition<B, T, Proof> extend, String anotherColumn, ColumnType<C> type, Function<T, C> getter) {
        this.base = extend.base;
        this.tableName = extend.tableName;
        this.foreignKeyColumn = extend.foreignKeyColumn;
        this.join = extend.join;

        this.moreTypes = new ArrayList<>(extend.moreTypes);
        this.moreTypes.add(type);
        this.moreGetters = new ArrayList<>(extend.moreGetters);
        this.moreGetters.add(getter);

        moreColumnNames = extend.moreColumnNames + "," + anotherColumn;
        moreInsertPlaceholders = extend.moreInsertPlaceholders + ",?";
        if (extend.moreUpdatePlaceholders.isEmpty()) moreUpdatePlaceholders = anotherColumn + "=?";
        else moreUpdatePlaceholders = extend.moreUpdatePlaceholders + "," + anotherColumn + "=?";
    }

    public <C> SpecialisationTableDefinition<B, T, Proof> withColumn(String anotherColumn, ColumnType<C> type, Function<T, C> getter) {
        return new SpecialisationTableDefinition<>(this, anotherColumn, type, getter);
    }

    public String getBaseSelectSQL() {
        return "SELECT ID," + base.columnNames + moreColumnNames + " FROM " + join;
    }

    public int getNumberOfBaseColumns() {
        return base.getNumberOfColumns();
    }

    public String getInsertSQL() {
        return "INSERT INTO " + tableName + " (" + foreignKeyColumn + moreColumnNames + ") VALUES (?" + moreInsertPlaceholders + ");";
    }

    public void setInsertParams(PreparedStatement statement, B base, Object[] params) throws SQLException {
        statement.setInt(1, base.getId());
        for (int i = 0; i < params.length; i++) {
            moreTypes.get(i).apply(statement, i + 2, params[i]);
        }
    }

    public String getUpdateSQL() {
        return "UPDATE " + tableName + " SET " + moreUpdatePlaceholders + " WHERE " + foreignKeyColumn + "=?";
    }

    public void setUpdateParams(PreparedStatement statement, T object) throws SQLException {
        for (int i = 0; i < moreTypes.size(); i++) {
            moreTypes.get(i).apply(statement, i + 1, moreGetters.get(i).apply(object));
        }
        statement.setInt(moreTypes.size() + 1, object.getId());
    }
}
