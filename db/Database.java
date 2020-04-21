package de.julianpadawan.common.db;

import org.apache.commons.dbcp2.BasicDataSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * holds a connection pool
 */
public final class Database {
    private static BiConsumer<String, Throwable> errorHandler = (sql, error) -> {
        System.out.println(sql);
        error.printStackTrace();
    };

    private static BasicDataSource dataSource;

    private Database() {
    }

    public static void init(String url, String user, String password) {
        dataSource = new BasicDataSource();
        dataSource.setMinIdle(1);
        dataSource.setMaxIdle(10);
        dataSource.setMaxOpenPreparedStatements(100);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
    }

    public static void execFile(String filename) throws IOException {
        final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
        if (inputStream == null) return;
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            int c;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        final String file = textBuilder.toString();
        for (String sql : file.split(";")) {
            sql = sql.strip();
            if (!sql.isEmpty()) execute(sql, PreparedStatement::execute, null);
        }
    }

    public static <R> R execute(String sql, SQLFunction<PreparedStatement, R> executor, R errorValue) {
        try (final Connection connection = getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            return executor.apply(statement);
        } catch (SQLException e) {
            errorHandler.accept(sql, e);
            return errorValue;
        }
    }

    private static Connection getConnection() throws SQLException {
        if (dataSource == null) throw new IllegalStateException("dataSource not initialized");
        return dataSource.getConnection();
    }

    public static void setErrorHandler(BiConsumer<String, Throwable> errorHandler) {
        Database.errorHandler = Objects.requireNonNull(errorHandler);
    }

}
