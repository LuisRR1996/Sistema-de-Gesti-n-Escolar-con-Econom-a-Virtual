package com.mycompany.escuelavirtual.util.db;

import com.mycompany.escuelavirtual.config.Config;
import java.sql.*;

public final class DatabaseConnection {
    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = Config.require("DB_URL");
        String user = Config.get("DB_USER", "");
        String password = Config.get("DB_PASSWORD", "");
        return DriverManager.getConnection(url, user, password);
    }

    public static boolean isAvailable() {
        try (Connection con = getConnection()) {
            return true;
        } catch (SQLException | IllegalStateException e) {
            return false;
        }
    }
}
