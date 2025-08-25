package com.hotel;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.*;
import java.sql.*;
public class BookingServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        String name    = req.getParameter("customerName");
        String roomIdS = req.getParameter("roomId");
        String checkIn = req.getParameter("checkIn");
        String checkOut= req.getParameter("checkOut");

        try (PrintWriter out = resp.getWriter()) {
            if (name == null || name.isBlank() ||
                roomIdS == null || roomIdS.isBlank() ||
                checkIn == null || checkIn.isBlank() ||
                checkOut == null || checkOut.isBlank()) {
                resp.setStatus(400);
                out.print("{\"status\":\"error\",\"msg\":\"All fields are required\"}");
                return;
            }

            int roomId = Integer.parseInt(roomIdS);

            try (Connection conn = DBUtil.getConnection()) {
                conn.setAutoCommit(false);

                // Check if available
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT is_booked FROM rooms WHERE room_id=? FOR UPDATE")) {
                    ps.setInt(1, roomId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            out.print("{\"status\":\"error\",\"msg\":\"Room not found\"}");
                            return;
                        }
                        if (rs.getBoolean("is_booked")) {
                            conn.rollback();
                            out.print("{\"status\":\"error\",\"msg\":\"Room already booked\"}");
                            return;
                        }
                    }
                }

                // Insert booking
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO bookings (room_id, customer_name, check_in, check_out) VALUES (?,?,?,?)")) {
                    ps.setInt(1, roomId);
                    ps.setString(2, name.trim());
                    ps.setString(3, checkIn);
                    ps.setString(4, checkOut);
                    ps.executeUpdate();
                }

                // Mark as booked
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE rooms SET is_booked=TRUE WHERE room_id=?")) {
                    ps.setInt(1, roomId);
                    ps.executeUpdate();
                }

                conn.commit();
                out.print("{\"status\":\"success\",\"msg\":\"Room booked successfully\"}");
            } catch (SQLException ex) {
                resp.setStatus(500);
                out.print("{\"status\":\"error\",\"msg\":\"Database error\"}");
                ex.printStackTrace();
            }
        }
    }
}
