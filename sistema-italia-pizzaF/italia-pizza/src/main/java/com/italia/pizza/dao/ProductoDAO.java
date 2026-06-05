package com.italia.pizza.dao;

import com.italia.pizza.config.DBConnection;
import com.italia.pizza.models.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> obtenerTodos() throws SQLException {
        String sql = """
                SELECT id_producto, codigo, nombre, descripcion,
                       precio, restricciones, cantidad, estatus
                FROM PRODUCTO
                """;
        List<Producto> lista = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new Producto(
                        rs.getInt("id_producto"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio"),
                        rs.getString("restricciones"),
                        rs.getInt("cantidad"),
                        rs.getString("estatus")
                ));
            }
        }
        return lista;
    }

    public boolean insertar(Producto p) throws SQLException {
        String sql = """
                INSERT INTO PRODUCTO
                    (codigo, nombre, descripcion, precio, restricciones, cantidad, estatus)
                VALUES (?, ?, ?, ?, ?, ?, 'Activo')
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getCodigo());
            stmt.setString(2, p.getNombre());
            stmt.setString(3, p.getDescripcion());
            stmt.setDouble(4, p.getPrecio());
            stmt.setString(5, p.getRestricciones().isBlank() ? null : p.getRestricciones());
            stmt.setInt(6,    p.getCantidad());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Producto p) throws SQLException {
        String sql = """
                UPDATE PRODUCTO
                SET codigo = ?, nombre = ?, descripcion = ?,
                    precio = ?, restricciones = ?, cantidad = ?
                WHERE id_producto = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getCodigo());
            stmt.setString(2, p.getNombre());
            stmt.setString(3, p.getDescripcion());
            stmt.setDouble(4, p.getPrecio());
            stmt.setString(5, p.getRestricciones().isBlank() ? null : p.getRestricciones());
            stmt.setInt(6,    p.getCantidad());
            stmt.setInt(7,    p.getIdProducto());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean desactivar(int id) throws SQLException {
        String sql = "UPDATE PRODUCTO SET estatus = 'Inactivo' WHERE id_producto = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean tieneVentas(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM detalle_pedido WHERE id_producto = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
}