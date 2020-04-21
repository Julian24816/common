package de.julianpadawan.common.db;

import java.sql.SQLException;

public interface SQLFunction<P, R> {
    R apply(P value) throws SQLException;
}
