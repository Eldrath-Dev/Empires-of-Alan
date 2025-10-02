package com.alan.empiresOfAlan.database;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.util.AsyncExecutor;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public class SQLiteManager {
    private final EmpiresOfAlan plugin;
    private final String dbFilePath;
    private Connection connection;
    private AsyncExecutor asyncExecutor;

    public SQLiteManager(EmpiresOfAlan plugin) {
        this.plugin = plugin;
        this.dbFilePath = new File(plugin.getDataFolder(), "database.db").getAbsolutePath();
        this.asyncExecutor = plugin.getAsyncExecutor();
    }

    /**
     * Initialize the database connection
     *
     * @return CompletableFuture that completes when initialization is done
     */
    public CompletableFuture<Void> initialize() {
        return asyncExecutor.runAsync(() -> {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
                createTables();
                plugin.getLogger().info("Database connection established successfully.");
            } catch (ClassNotFoundException | SQLException e) {
                plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Create the database tables
     *
     * @throws SQLException if an error occurs
     */
    private void createTables() throws SQLException {
        // Create residents table
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS residents (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "name TEXT, " +
                    "town_id TEXT, " +
                    "town_role INTEGER, " +
                    "nation_id TEXT, " +
                    "nation_role INTEGER, " +
                    "last_online BIGINT" +
                    ")");
        }

        // Create towns table
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS towns (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT UNIQUE, " +
                    "owner_id TEXT, " +
                    "balance REAL, " +
                    "nation_id TEXT, " +
                    "spawn_world TEXT, " +
                    "spawn_x DOUBLE, " +
                    "spawn_y DOUBLE, " +
                    "spawn_z DOUBLE, " +
                    "spawn_yaw FLOAT, " +
                    "spawn_pitch FLOAT, " +
                    "tax_rate REAL, " +
                    "last_tax_collection BIGINT, " +
                    "is_public BOOLEAN" +
                    ")");
        }

        // Create nations table
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS nations (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT UNIQUE, " +
                    "capital_id TEXT, " +
                    "leader_id TEXT, " +
                    "balance REAL, " +
                    "spawn_world TEXT, " +
                    "spawn_x DOUBLE, " +
                    "spawn_y DOUBLE, " +
                    "spawn_z DOUBLE, " +
                    "spawn_yaw FLOAT, " +
                    "spawn_pitch FLOAT, " +
                    "tax_rate REAL, " +
                    "last_tax_collection BIGINT, " +
                    "is_public BOOLEAN" +
                    ")");
        }

        // Create claims table
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS claims (" +
                    "id TEXT PRIMARY KEY, " +
                    "world TEXT, " +
                    "x INTEGER, " +
                    "z INTEGER, " +
                    "town_id TEXT, " +
                    "UNIQUE(world, x, z)" +
                    ")");
        }

        // Create claim flags table
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS claim_flags (" +
                    "claim_id TEXT, " +
                    "flag TEXT, " +
                    "value BOOLEAN, " +
                    "PRIMARY KEY (claim_id, flag)" +
                    ")");
        }
    }

    /**
     * Get the database connection
     *
     * @return The connection
     * @throws SQLException if an error occurs
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
        }
        return connection;
    }

    /**
     * Execute a query asynchronously
     *
     * @param sql SQL query
     * @param params Query parameters
     * @return CompletableFuture with the ResultSet
     */
    public CompletableFuture<ResultSet> query(String sql, Object... params) {
        return asyncExecutor.runAsync(() -> {
            try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                return stmt.executeQuery();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error executing query: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Execute an update asynchronously
     *
     * @param sql SQL update statement
     * @param params Update parameters
     * @return CompletableFuture with the number of affected rows
     */
    public CompletableFuture<Integer> update(String sql, Object... params) {
        return asyncExecutor.runAsync(() -> {
            try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                return stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error executing update: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Close the database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed.");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error closing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}