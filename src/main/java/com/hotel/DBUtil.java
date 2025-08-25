package com.hotel;

import java.sql.*;

public class DBUtil {
    private static final String URL  = "jdbc:mysql://localhost:3306/hotel_bd";
    private static final String USER = "root"; // change if needed
    private static final String PASS = "root"; // change if needed

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); 
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
