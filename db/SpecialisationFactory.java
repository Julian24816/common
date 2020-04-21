package de.julianpadawan.common.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class SpecialisationFactory<B extends ModelObject<B>, T extends B, Proof extends Specialisation<B, T>> {
    private final SpecialisationTableDefinition<B, T, Proof> definition;
    private final ModelFactory<B> factory;
    private final SQLBiFunction<B, ResultView, T> resultConverter;

    protected SpecialisationFactory(ModelFactory<B> factory,
                                    SQLBiFunction<B, ResultView, T> resultConverter,
                                    SpecialisationTableDefinition<B, T, Proof> definition) {
        this.factory = factory;
        this.resultConverter = resultConverter;
        this.definition = definition;
    }

    public Collection<T> getAll() {
        return selectWhere(this::selectAll, null, 0, null);
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

    protected final Collection<T> selectAll(ResultSet resultSet) throws SQLException {
        final ResultView view = new ResultView(resultSet);
        final List<T> list = new LinkedList<>();
        while (resultSet.next()) list.add(get(view));
        return list;
    }

    private T get(ResultView view) throws SQLException {
        return resultConverter.apply(factory.resultConverter.apply(view), view);
    }

    public boolean update(T object) {
        if (!factory.update(object)) return false;
        final String sql = definition.getUpdateSQL();
        return Database.execute(sql, statement -> {
            definition.setUpdateParams(statement, object);
            final int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        }, false);
    }

    public T createNew(Object... params) {
        final Object[] baseParams = Arrays.copyOfRange(params, 0, definition.getNumberOfBaseColumns());
        final Object[] specialisationParams = Arrays.copyOfRange(params, definition.getNumberOfBaseColumns(), params.length);

        final B newBase = factory.createNew(baseParams);
        if (newBase == null) return null;

        final String sql = definition.getInsertSQL();
        return Database.execute(sql, statement -> {
            definition.setInsertParams(statement, newBase, specialisationParams);
            statement.execute();
            return getForId(newBase.getId());
        }, null);
    }

    public T getForId(int id) {
        return selectWhere(this::selectFirst, "id=?", 1, (preparedStatement, param) -> preparedStatement.setInt(param, id));
    }

    protected final T selectFirst(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) return get(new ResultView(resultSet));
        return null;
    }
}
