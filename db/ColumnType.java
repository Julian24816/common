package de.julianpadawan.common.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Function;

public final class ColumnType<T> {
    public static final ColumnType<Integer> INTEGER = new ColumnType<>(
            PreparedStatement::setInt,
            param -> tryCast(Integer.class, param));
    public static final ColumnType<Double> DOUBLE = new ColumnType<>(
            PreparedStatement::setDouble,
            param -> tryCast(Double.class, param));
    public static final ColumnType<String> STRING = new ColumnType<>(
            PreparedStatement::setString,
            param -> tryCast(String.class, param));
    public static final ColumnType<LocalDateTime> TIMESTAMP = new ColumnType<>(
            (statement, index, value) -> statement.setTimestamp(index,
                    value == null ? null : Timestamp.valueOf(value)),
            param -> tryCast(LocalDateTime.class, param));
    public static final ColumnType<Duration> DURATION = new ColumnType<>(
            (statement, index, value) -> {
                if (value == null) statement.setObject(index, null);
                else statement.setLong(index, value.toMinutes());
            },
            param -> tryCast(Duration.class, param));
    public static final ColumnType<Boolean> BOOLEAN = new ColumnType<>(
            PreparedStatement::setBoolean,
            param -> tryCast(Boolean.class, param));

    private final Function<Object, T> converter;
    private final StatementApplier<T> applier;

    private ColumnType(StatementApplier<T> applier, Function<Object, T> converter) {
        this.converter = converter;
        this.applier = applier;
    }

    public static <C extends ModelObject<C>> ColumnType<C> getForeignKeyColumn(Class<C> clazz) {
        return new ColumnType<>(
                (statement, index, value) -> {
                    if (value == null) statement.setObject(index, null);
                    else statement.setInt(index, value.getId());
                },
                param -> tryCast(clazz, param));
    }

    private static <T> T tryCast(Class<T> clazz, Object param) {
        try {
            return clazz.cast(param);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("required " + clazz.getName() + " but was " + param.getClass().getName());
        }
    }

    public void apply(PreparedStatement statement, int index, Object param) throws SQLException {
        applier.apply(statement, index, converter.apply(param));
    }

    private interface StatementApplier<T> {
        void apply(PreparedStatement statement, int index, T value) throws SQLException;
    }
}
