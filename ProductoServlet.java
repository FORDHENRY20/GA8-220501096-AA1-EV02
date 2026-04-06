package com.readycar.controller;

import com.readycar.config.ConexionBD;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.stream.Collectors;

@WebServlet("/api/productos/*")
public class ProductoServlet extends HttpServlet {

    private void setAccessControlHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json;charset=UTF-8");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setAccessControlHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setAccessControlHeaders(response);
        try (PrintWriter out = response.getWriter(); Connection con = ConexionBD.conectar()) {
            String sql = "SELECT * FROM productos";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            StringBuilder json = new StringBuilder("[");
            boolean primero = true;
            while (rs.next()) {
                if (!primero) json.append(",");
                json.append("{")
                    .append("\"id\":").append(rs.getInt("id")).append(",")
                    .append("\"nombre\":\"").append(rs.getString("nombre")).append("\",")
                    .append("\"descripcion\":\"").append(rs.getString("descripcion")).append("\",")
                    .append("\"precio\":").append(rs.getDouble("precio")).append(",")
                    .append("\"stock\":").append(rs.getInt("stock")).append(",")
                    .append("\"imagen\":\"").append(rs.getString("imagen")).append("\",")
                    .append("\"destacado\":").append(rs.getInt("destacado"))
                    .append("}");
                primero = false;
            }
            json.append("]");
            out.print(json.toString());
        } catch (Exception e) {
            response.setStatus(500);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setAccessControlHeaders(response);
        try (PrintWriter out = response.getWriter(); Connection con = ConexionBD.conectar()) {
            String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            
            String nombre = extraerDato(body, "nombre");
            String descripcion = extraerDato(body, "descripcion");
            String precioStr = extraerDato(body, "precio");
            String stockStr = extraerDato(body, "stock");
            String imagen = extraerDato(body, "imagen");
            String destacadoStr = extraerDato(body, "destacado");

            String sql = "INSERT INTO productos (nombre, descripcion, precio, stock, imagen, destacado) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setDouble(3, Double.parseDouble(precioStr));
            ps.setInt(4, Integer.parseInt(stockStr));
            ps.setString(5, (imagen == null || imagen.isEmpty()) ? "default.jpg" : imagen);
            ps.setInt(6, Integer.parseInt(destacadoStr));

            ps.executeUpdate();
            out.print("{\"mensaje\":\"Producto creado correctamente\"}");
        } catch (Exception e) {
            response.setStatus(500);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setAccessControlHeaders(response);
        try (PrintWriter out = response.getWriter(); Connection con = ConexionBD.conectar()) {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(400);
                out.print("{\"error\":\"ID requerido\"}");
                return;
            }
            int id = Integer.parseInt(pathInfo.substring(1));
            String sql = "DELETE FROM productos WHERE id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            if (ps.executeUpdate() > 0) {
                out.print("{\"mensaje\":\"Producto eliminado\"}");
            } else {
                response.setStatus(404);
                out.print("{\"error\":\"No encontrado\"}");
            }
        } catch (Exception e) {
            response.setStatus(500);
            // Aquí corregí el error de la variable 'out' que te salía en la consola
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private String extraerDato(String json, String campo) {
        try {
            String clave = "\"" + campo + "\":";
            int inicio = json.indexOf(clave) + clave.length();
            int fin = json.indexOf(",", inicio);
            if (fin == -1) fin = json.indexOf("}", inicio);
            return json.substring(inicio, fin).replace("\"", "").replace(":", "").trim();
        } catch (Exception e) { return ""; }
    }
}