package de.julianpadawan.common.db;

import java.sql.ResultSet;
import java.util.Optional;

public abstract class ExtensionFactory<B extends ModelObject<B>, T extends Extension<B>> {

    private final SQLBiFunction<B, ResultView, T> resultConverter;
    private final ExtensionTableDefinition<B, T> definition;

    protected ExtensionFactory(SQLBiFunction<B, ResultView, T> resultConverter,
                               ExtensionTableDefinition<B, T> definition) {
        this.resultConverter = resultConverter;
        this.definition = definition;
    }

    public Optional<T> get(B base) {
        String sql = definition.getSelectSQL();
        return Optional.ofNullable(Database.execute(sql, statement -> {
            statement.setInt(1, base.getId());
            try (final ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultConverter.apply(base, new ResultView(resultSet));
                return null;
            }
        }, null));
    }
}
