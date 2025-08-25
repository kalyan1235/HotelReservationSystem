package com.hotel;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.*;
import java.sql.*;

public class CancelServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        String bookingIdS = req.getParameter("bookingId");

        try (PrintWriter out = resp.getWriter()) {
            if (bookingIdS == null || bookingIdS.isBlank()) {
                resp.setStatus(400);
                out.print("{\"status\":\"error\",\"msg\":\"bookingId is required\"}");
                return;
            }
            int bookingId = Integer.parseInt(bookingIdS);

            try (Connection conn = DBUtil.getConnection()) {
                conn.setAutoCommit(false);

                Integer roomId = null;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT room_id FROM bookings WHERE booking_id=? FOR UPDATE")) {
                    ps.setInt(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) roomId = rs.getInt("room_id");
                    }
                }

                if (roomId == null) {
                    conn.rollback();
                    out.print("{\"status\":\"error\",\"msg\":\"No booking found\"}");
                    return;
                }

                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM bookings WHERE booking_id=?")) {
                    del.setInt(1, bookingId);
                    del.executeUpdate();
                }

                try (PreparedStatement upd = conn.prepareStatement(
                        "UPDATE rooms SET is_booked=FALSE WHERE room_id=?")) {
                    upd.setInt(1, roomId);
                    upd.executeUpdate();
                }

                conn.commit();
                out.print("{\"status\":\"success\",\"msg\":\"Booking cancelled\"}");
            } catch (SQLException ex) {
                resp.setStatus(500);
                out.print("{\"status\":\"error\",\"msg\":\"Database error\"}");
                ex.printStackTrace();
            }
        }
    }
}
