
package com.flowershop;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/contact")
public class ContactServlet extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest request,
                             HttpServletResponse response)
            throws IOException {

        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");

        request.setCharacterEncoding("UTF-8");

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String message = request.getParameter("message");

        // Validate required fields
        if (name == null || name.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || message == null || message.trim().isEmpty()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                    "{\"error\":\"Please fill in all required fields\"}");
            return;
        }

        name = name.trim();
        email = email.trim();
        phone = phone == null ? "" : phone.trim();
        message = message.trim();

        if (name.length() > 100 || email.length() > 150
                || phone.length() > 25 || message.length() > 2000) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                    "{\"error\":\"One or more fields exceed the allowed length\"}");
            return;
        }

        String sql = "INSERT INTO contact_messages "
                + "(name, email, phone, message) VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, phone.isEmpty() ? null : phone);
            statement.setString(4, message);

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(
                        "{\"success\":true,"
                        + "\"message\":\"Your message has been sent successfully\"}");
            } else {
                response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(
                        "{\"error\":\"Unable to save your message\"}");
            }

        } catch (SQLException e) {
            getServletContext().log("Could not save contact message", e);

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                    "{\"error\":\"Something went wrong. Please try again.\"}");
        }
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader(
                "Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader(
                "Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader(
                "Access-Control-Allow-Headers", "Content-Type");
    }
}
