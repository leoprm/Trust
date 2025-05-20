package com.mycompany.trust;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Manages database connections using a connection pool
 */
public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static HikariDataSource dataSource;
    
    // Default database configuration
    private static String DB_URL = "jdbc:mysql://localhost:3306/trust_system";
    private static String DB_USER = "root";
    private static String DB_PASSWORD = "root";
    
    /**
     * Set database connection properties
     * 
     * @param url Database URL
     * @param user Database username
     * @param password Database password
     */
    public static void setDbCredentials(String url, String user, String password) {
        DB_URL = url;
        DB_USER = user;
        DB_PASSWORD = password;
        LOGGER.info("Database credentials updated");
    }
    
    /**
     * Initializes the database connection pool
     * @throws SQLException if there's an error setting up the connection pool
     */
    public static void initializeDataSource() throws SQLException {
        if (dataSource == null) {
            try {
                // Try to load credentials from environment variables or system properties
                String envDbUrl = System.getenv("DB_URL");
                String envDbUser = System.getenv("DB_USER");
                String envDbPassword = System.getenv("DB_PASSWORD");
                
                if (envDbUrl != null && !envDbUrl.isEmpty()) {
                    DB_URL = envDbUrl;
                }
                if (envDbUser != null && !envDbUser.isEmpty()) {
                    DB_USER = envDbUser;
                }
                if (envDbPassword != null && !envDbPassword.isEmpty()) {
                    DB_PASSWORD = envDbPassword;
                }
                
                // Also check system properties
                String sysDbUrl = System.getProperty("db.url");
                String sysDbUser = System.getProperty("db.user");
                String sysDbPassword = System.getProperty("db.password");
                
                if (sysDbUrl != null && !sysDbUrl.isEmpty()) {
                    DB_URL = sysDbUrl;
                }
                if (sysDbUser != null && !sysDbUser.isEmpty()) {
                    DB_USER = sysDbUser;
                }
                if (sysDbPassword != null && !sysDbPassword.isEmpty()) {
                    DB_PASSWORD = sysDbPassword;
                }
                
                LOGGER.info("Initializing database connection pool with URL: " + DB_URL + ", User: " + DB_USER);
                
                // Create HikariCP config
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(DB_URL);
                config.setUsername(DB_USER);
                config.setPassword(DB_PASSWORD);
                
                // Connection pool settings
                config.setMaximumPoolSize(10);
                config.setMinimumIdle(2);
                config.setIdleTimeout(30000);
                config.setMaxLifetime(1800000);
                config.setConnectionTimeout(30000);
                
                // MySQL specific settings
                Properties props = new Properties();
                props.setProperty("cachePrepStmts", "true");
                props.setProperty("prepStmtCacheSize", "250");
                props.setProperty("prepStmtCacheSqlLimit", "2048");
                props.setProperty("useServerPrepStmts", "true");
                props.setProperty("useLocalSessionState", "true");
                props.setProperty("rewriteBatchedStatements", "true");
                props.setProperty("cacheResultSetMetadata", "true");
                props.setProperty("cacheServerConfiguration", "true");
                props.setProperty("elideSetAutoCommits", "true");
                props.setProperty("maintainTimeStats", "false");
                props.setProperty("autoReconnect", "true");
                config.setDataSourceProperties(props);
                
                // Create the data source
                dataSource = new HikariDataSource(config);
                
                // Test connection
                try (Connection conn = dataSource.getConnection()) {
                    if (conn.isValid(5)) {
                        LOGGER.info("Database connection pool initialized successfully.");
                    } else {
                        throw new SQLException("Could not establish database connection");
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error initializing database connection pool", e);
                throw e;
            }
        }
    }
    
    /**
     * Gets a connection from the connection pool
     * @return a database connection
     * @throws SQLException if there's an error getting a connection
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource not initialized. Call initializeDataSource() first.");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Closes the connection pool
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            LOGGER.info("Closing database connection pool...");
            dataSource.close();
            dataSource = null;
            LOGGER.info("Database connection pool closed.");
        }
    }
}