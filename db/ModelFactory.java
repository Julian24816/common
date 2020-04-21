package de.julianpadawan.common.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class ModelFactory<T extends ModelObject<T>> {

    protected final ModelTableDefinition<T> definition;
    final SQLFunction<ResultView, T> resultConverter;

    protected ModelFactory(SQLFunction<ResultView, T> resultConverter, ModelTableDefinition<T> definition) {
        this.resultConverter = resultConverter;
        this.definition = definition;
    }

    public boolean update(T obj) {
        final String sql = definition.getUpdateSQL();
        return Database.execute(sql, statement -> {
            definition.setUpdateParams(statement, obj);
            final int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        }, false);
    }

    public T createNew(Object... params) {
        final String sql = definition.getInsertSQL();
        return Database.execute(sql, statement -> {
            definition.setInsertParams(statement, params);
            statement.execute();
            try (final ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) return getForId(generatedKeys.getInt(1));
            }
            return null;
        }, null);
    }

    public T getForId(int id) {
        return selectWhere(this::selectFirst, "id=?", 1, (preparedStatement, param) -> preparedStatement.setInt(param, id));
    }

    protected final <R> R selectWhere(SQLFunction<ResultSet, R> selector, String where, int params, SQLBiConsumer<PreparedStatement, Integer> paramSetter) {
        if (params < 0) throw new IllegalArgumentException("params must be >= 0");
        String sql = definition.getBaseSelectSQL();
        if (where != null && !where.isEmpty()) sql += " WHERE " + where;
        return Database.execute(sql, statement -> {
            for (int i = 1; i <= params; i++) paramSetter.accept(statement, i);
            try (final ResultSet resultSet = statement.executeQuery()) {
                return selector.apply(resultSet);
            }
        }, null);
    }

    protected final T selectFirst(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) return resultConverter.apply(new ResultView(resultSet));
        return null;
    }

    public Collection<T> getAll() {
        return selectWhere(this::selectAll, null);
    }

    protected final <R> R selectWhere(SQLFunction<ResultSet, R> selector, String where) {
        return selectWhere(selector, where, 0, null);
    }

    protected final Collection<T> selectAll(ResultSet resultSet) throws SQLException {
        final ResultView view = new ResultView(resultSet);
        final List<T> list = new LinkedList<>();
        while (resultSet.next()) list.add(resultConverter.apply(view));
        return list;
    }

}
