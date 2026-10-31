
package com.flowershop;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/products")
public class ProductServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");

        response.setContentType("application/json;charset=UTF-8");

        String sql = "SELECT id, name, price, image_url FROM products";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            while (result.next()) {

                if (!first) {
                    json.append(",");
                }

                json.append("{")
                    .append("\"id\":\"")
                    .append(escapeJson(result.getString("id")))
                    .append("\",")
                    .append("\"name\":\"")
                    .append(escapeJson(result.getString("name")))
                    .append("\",")
                    .append("\"price\":")
                    .append(result.getBigDecimal("price").toPlainString())
                    .append(",")
                    .append("\"image\":\"")
                    .append(escapeJson(result.getString("image_url")))
                    .append("\"")
                    .append("}");

                first = false;
            }

            json.append("]");
            response.getWriter().write(json.toString());

        } catch (SQLException e) {
            getServletContext().log("Could not retrieve products", e);
            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                    "{\"error\":\"Unable to load products\"}");
        }
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
