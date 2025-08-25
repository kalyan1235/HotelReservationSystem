package com.hotel;

import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.*;
import java.sql.*;

public class ReservationsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        String sql = """
            SELECT b.booking_id, r.room_number, r.type, b.customer_name, b.check_in, b.check_out
            FROM bookings b
            JOIN rooms r ON r.room_id = b.room_id
            ORDER BY b.booking_id
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
             PrintWriter out = resp.getWriter()) {

            StringBuilder sb = new StringBuilder("[");
            while (rs.next()) {
                if (sb.length() > 1) sb.append(',');
                sb.append("{")
                  .append("\"bookingId\":").append(rs.getInt("booking_id")).append(',')
                  .append("\"roomNumber\":\"").append(rs.getString("room_number")).append("\",")
                  .append("\"type\":\"").append(rs.getString("type")).append("\",")
                  .append("\"customer\":\"").append(rs.getString("customer_name")).append("\",")
                  .append("\"checkIn\":\"").append(rs.getDate("check_in")).append("\",")
                  .append("\"checkOut\":\"").append(rs.getDate("check_out")).append("\"")
                  .append("}");
            }
            sb.append("]");
            out.print(sb.toString());
        } catch (SQLException e) {
            resp.setStatus(500);
            resp.getWriter().print("[]");
            e.printStackTrace();
        }
    }
}
