package org.innoswp.innotable.model.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

// Works on Java 8+
public final class ConnectionManager {

    private static final Properties properties = new Properties();

    static {
        try {
            properties.load(new BufferedReader(new FileReader("database_config.properties")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String URL_KEY = "db.url";
    private static final String USERNAME_KEY = "db.username";
    private static final String PASSWORD_KEY = "db.password";

    private ConnectionManager() {
    }

    public static Connection open() {
        try {
            return DriverManager.getConnection(
                    properties.getProperty(URL_KEY),
                    properties.getProperty(USERNAME_KEY),
                    properties.getProperty(PASSWORD_KEY)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
