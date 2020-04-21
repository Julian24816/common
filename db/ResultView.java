package de.julianpadawan.common.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ResultView {
    private final Map<String, Integer> map = new HashMap<>();
    private final ResultSet resultSet;

    public ResultView(ResultSet resultSet) {
        this.resultSet = Objects.requireNonNull(resultSet);
    }

    public String getString(String key) throws SQLException {
        return resultSet.getString(getIndex(key));
    }

    private int getIndex(String key) throws SQLException {
        if (!map.containsKey(key)) map.put(key, resultSet.findColumn(key));
        return map.get(key);
    }

    public int getInt(String key) throws SQLException {
        return resultSet.getInt(getIndex(key));
    }

    public double getDouble(String key) throws SQLException {
        return resultSet.getDouble(getIndex(key));
    }

    public Duration getDuration(String key) throws SQLException {
        return Duration.ofMinutes(getLong(key));
    }

    public LocalDateTime getDateTime(String key) throws SQLException {
        final Timestamp timestamp = resultSet.getTimestamp(getIndex(key));
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public long getLong(String key) throws SQLException {
        return resultSet.getLong(getIndex(key));
    }

    public Optional<Integer> getOptionalInt(String key) throws SQLException {
        int result = resultSet.getInt(getIndex(key));
        if (resultSet.wasNull()) return Optional.empty();
        return Optional.of(result);
    }

    public boolean getBoolean(String key) throws SQLException {
        return resultSet.getBoolean(getIndex(key));
    }
}
