package de.julianpadawan.common.db;

import org.apache.commons.dbcp2.BasicDataSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * holds a connection pool
 */
public final class Database {
    private static Consumer<String> sqlOnJavaFXApplicationThreadCallback = (sql) -> {
        System.err.printf("JavaFX Application Thread executing SQL: %s\n", sql);
        Thread.dumpStack();
    };
    private static BiConsumer<String, Throwable> errorHandler = (sql, error) -> {
        System.out.println(sql);
        error.printStackTrace();
    };

    private static CountDownLatch initialized;

    private static BasicDataSource dataSource;
    private static Runnable onAwaitCallback;

    private Database() {
    }

    public static void prepareInitAsync(Runnable onAwaitCallback) {
        synchronized (Database.class) {
            if (dataSource != null) throw new IllegalArgumentException("already initialized");
            Database.onAwaitCallback = onAwaitCallback;
            initialized = new CountDownLatch(1);
        }
    }

    public static void init(String url, String user, String password) {
        synchronized (Database.class) {
            dataSource = new BasicDataSource();
            dataSource.setMinIdle(1);
            dataSource.setMaxIdle(10);
            dataSource.setMaxOpenPreparedStatements(100);
            dataSource.setUrl(url);
            dataSource.setUsername(user);
            dataSource.setPassword(password);

            if (initialized != null) initialized.countDown();
        }
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
             final PreparedStatement statement = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            if (Thread.currentThread().getName().equals("JavaFX Application Thread") && sqlOnJavaFXApplicationThreadCallback != null)
                sqlOnJavaFXApplicationThreadCallback.accept(sql);
            return executor.apply(statement);
        } catch (SQLException e) {
            errorHandler.accept(sql, e);
            return errorValue;
        }
    }

    private static Connection getConnection() throws SQLException {
        if (dataSource == null && initialized == null) throw new IllegalStateException("dataSource not initialized");
        if (initialized != null) try {
            if (initialized.getCount() == 1 && onAwaitCallback != null) onAwaitCallback.run();
            initialized.await();
        } catch (InterruptedException e) {
            errorHandler.accept("connection await interrupted", e);
            throw new IllegalStateException("await interrupted");
        }
        return dataSource.getConnection();
    }

    //TODO remove sqlite specific code
    public static int queryPragma(String name) {
        return queryPragma(name, -1);
    }

    public static int queryPragma(String name, int errorValue) {
        return execute(String.format("pragma %s;", name), statement -> {
            try (final ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : errorValue;
            }
        }, errorValue);
    }

    public static boolean setPragma(String name, int value) {
        return execute(String.format("pragma %s=%d;", name, value), statement -> statement.executeUpdate() > 0, false);
    }

    public static void setErrorHandler(BiConsumer<String, Throwable> errorHandler) {
        Database.errorHandler = Objects.requireNonNull(errorHandler);
    }

    public static void setSqlOnJavaFXApplicationThreadCallback(final Consumer<String> callback) {
        sqlOnJavaFXApplicationThreadCallback = callback;
    }

    public static void close() throws SQLException {
        if (dataSource != null) dataSource.close();
    }
}
