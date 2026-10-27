package com.mycompany.escuelavirtual;

import com.mycompany.escuelavirtual.util.db.DatabaseConnection;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        if (!DatabaseConnection.isAvailable()) {
            System.err.println("No se pudo verificar la conexion con SQL Server");
            return;
        }
        System.out.println("Conexion a SQL Server exitosa");
    }
}
