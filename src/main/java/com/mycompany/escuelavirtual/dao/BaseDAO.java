package com.mycompany.escuelavirtual.dao;

import com.mycompany.escuelavirtual.util.db.DatabaseConnection;
import java.sql.*;

public abstract class BaseDAO {
    protected Connection connection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    protected void close(AutoCloseable resource) {
        if (resource != null)
            try {
                resource.close();
            } catch (Exception ignored) {
            }
    }
}
