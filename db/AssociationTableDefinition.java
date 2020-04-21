package de.julianpadawan.common.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class AssociationTableDefinition<A extends ModelObject<A>, B extends ModelObject<B>, T extends Association<A, B>> {
    private final String tableName;

    private final String firstColumn;
    private final String firstColumnPlaceholder;
    private final ColumnType<A> firstColumnType;

    private final String secondColumn;
    private final String secondColumnPlaceholder;
    private final ColumnType<B> secondColumnType;

    public AssociationTableDefinition(String tableName,
                                      String firstColumn, Class<A> firstColumnType,
                                      String secondColumn, Class<B> secondColumnType) {
        this.tableName = tableName;

        this.firstColumn = firstColumn;
        this.firstColumnPlaceholder = firstColumn + "=?";
        this.firstColumnType = ColumnType.getForeignKeyColumn(firstColumnType);

        this.secondColumn = secondColumn;
        this.secondColumnPlaceholder = secondColumn + "=?";
        this.secondColumnType = ColumnType.getForeignKeyColumn(secondColumnType);
    }

    public String getDeleteSQL() {
        return "DELETE FROM " + tableName + " WHERE " + firstColumnPlaceholder + " AND " + secondColumnPlaceholder;
    }

    public String getSelectAllOfFirstSQL() {
        return "SELECT " + secondColumn + " FROM " + tableName + " WHERE " + firstColumnPlaceholder;
    }

    public String getSecondColumnName() {
        return secondColumn;
    }


    String getInsertSQL() {
        return "INSERT INTO " + tableName + "(" + firstColumn + "," + secondColumn + ") VALUES (?,?)";
    }

    void setSQLParams(PreparedStatement statement, A first, B second) throws SQLException {
        firstColumnType.apply(statement, 1, first);
        secondColumnType.apply(statement, 2, second);
    }

    void setSQLParams(PreparedStatement statement, T obj) throws SQLException {
        int getter = 0;
        firstColumnType.apply(statement, 1, obj.getFirst());
        secondColumnType.apply(statement, 2, obj.getSecond());
    }

    public String getCountSQL() {
        return "SELECT COUNT(*) FROM " + tableName + " WHERE " + firstColumnPlaceholder + " AND " + secondColumnPlaceholder;
    }
}
