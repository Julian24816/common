package de.julianpadawan.common.db;

import java.sql.SQLException;

public interface SQLBiFunction<P, Q, R> {
    R apply(P first, Q second) throws SQLException;
}
