package me.vasir.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.vasir.manager.ConfigManager;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariCP {

    private static final HikariConfig config = new HikariConfig();
    private static final ConfigManager configManager = new ConfigManager();
    private static final HikariDataSource ds;

    static {
        config.setJdbcUrl(configManager.getConfig("url"));
        config.setUsername(configManager.getConfig("user"));
        config.setPassword(configManager.getConfig("password"));

        // HikariCP yapılandırması
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setIdleTimeout(30000);
        config.setMaxLifetime(1800000);
        config.setConnectionTimeout(10000);

        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
