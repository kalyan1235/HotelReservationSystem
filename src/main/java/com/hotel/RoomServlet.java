package com.hotel;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.*;
import java.sql.*;

public class RoomServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        String sql = "SELECT room_id, room_number, type, price FROM rooms WHERE is_booked = FALSE ORDER BY room_number";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
             PrintWriter out = resp.getWriter()) {

            StringBuilder sb = new StringBuilder("[");
            while (rs.next()) {
                if (sb.length() > 1) sb.append(',');
                sb.append("{")
                  .append("\"roomId\":").append(rs.getInt("room_id")).append(',')
                  .append("\"roomNumber\":\"").append(rs.getString("room_number")).append("\",")
                  .append("\"type\":\"").append(rs.getString("type")).append("\",")
                  .append("\"price\":").append(rs.getBigDecimal("price"))
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
