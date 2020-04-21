package de.julianpadawan.common.db;

import java.sql.SQLException;

public interface SQLBiConsumer<T, U> {
    void accept(T first, U second) throws SQLException;
}
