package bank.DB;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DbManager {

    private static final String PROP_URL_KEY  = "db.url";
    private static final String PROP_USER_KEY = "db.user";
    private static final String PROP_PASS_KEY = "db.password";

    private static final String DEFAULT_CONFIG_PATH = "config/db.properties";

    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;

    static {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found on classpath", e);
        }

        // Determine config path:
        // 1) -Dbankdb.config=/path/to/db.properties (system property)
        // 2) fallback: ./db.properties in working directory
        String configPathStr = System.getProperty("bankdb.config", DEFAULT_CONFIG_PATH);
        Path configPath = Paths.get(configPathStr);

        if (!Files.exists(configPath)) {
            throw new RuntimeException(
                "Database config file not found at: " + configPath.toAbsolutePath() +
                " (set -Dbankdb.config=/path/to/db.properties or create db.properties)"
            );
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load DB config file: " + configPath.toAbsolutePath(), e);
        }

        DB_URL      = props.getProperty(PROP_URL_KEY);
        DB_USER     = props.getProperty(PROP_USER_KEY);
        DB_PASSWORD = props.getProperty(PROP_PASS_KEY);

        if (DB_URL == null || DB_USER == null || DB_PASSWORD == null) {
            throw new RuntimeException(
                "db.url, db.user, or db.password missing in " + configPath.toAbsolutePath()
            );
        }
    }

    private DbManager() {
        // utility class
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

}