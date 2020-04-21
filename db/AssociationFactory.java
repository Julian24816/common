package de.julianpadawan.common.db;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

public abstract class AssociationFactory<A extends ModelObject<A>, B extends ModelObject<B>, T extends Association<A, B>> {
    private final BiFunction<A, B, T> constructor;
    private final ModelFactory<B> bFactory;
    private final AssociationTableDefinition<A, B, T> definition;

    protected AssociationFactory(BiFunction<A, B, T> constructor, ModelFactory<B> bFactory, AssociationTableDefinition<A, B, T> definition) {
        this.constructor = constructor;
        this.bFactory = bFactory;
        this.definition = definition;
    }

    public Collection<T> getAll(A first) {
        String sql = definition.getSelectAllOfFirstSQL();
        return Database.execute(sql, statement -> {
            statement.setInt(1, first.getId());
            try (final ResultSet resultSet = statement.executeQuery()) {
                final ResultView view = new ResultView(resultSet);
                final List<T> list = new LinkedList<>();
                while (resultSet.next()) list.add(constructor.apply(
                        first,
                        bFactory.getForId(view.getInt(definition.getSecondColumnName()))
                ));
                return list;
            }
        }, Collections.emptyList());
    }

    public T create(A first, B second) {
        String sql = definition.getInsertSQL();
        return Database.execute(sql, statement -> {
            definition.setSQLParams(statement, first, second);
            statement.execute();
            return constructor.apply(first, second);
        }, null);
    }

    public boolean delete(T association) {
        String sql = definition.getDeleteSQL();
        return Database.execute(sql, statement -> {
            definition.setSQLParams(statement, association);
            final int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        }, false);
    }

    public boolean exists(A first, B second) {
        String sql = definition.getCountSQL();
        return Database.execute(sql, statement -> {
            definition.setSQLParams(statement, first, second);
            final ResultSet resultSet = statement.executeQuery();
            return resultSet.next() && resultSet.getInt(1) > 0;
        }, false);
    }
}
