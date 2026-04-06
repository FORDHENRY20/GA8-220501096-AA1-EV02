package com.readycar.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String URL = "jdbc:mysql://localhost:3306/readycardb?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = ""; // Cambia si pusiste contraseña en XAMPP

    public static Connection conectar() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Error: No se encontró el driver de MySQL", e);
        }
    }
}