package com.italia.pizza.dao;

import com.italia.pizza.config.DBConnection;
import com.italia.pizza.models.DetallePedido;
import com.italia.pizza.models.Pedido;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    // ── LISTAR TODOS ──────────────────────────────────────────
    public List<Pedido> obtenerTodos() throws SQLException {
        String sql = """
                SELECT p.id_pedido, p.fecha, p.total, p.estatus_actual,
                       p.id_cliente,
                       CONCAT(c.nombre, ' ', c.apellidos) AS nombre_cliente,
                       p.id_empleado,
                       CONCAT(e.nombre, ' ', e.apellidos) AS nombre_empleado
                FROM PEDIDO p
                JOIN USUARIO c ON c.id_usuario = p.id_cliente
                JOIN USUARIO e ON e.id_usuario = p.id_empleado
                ORDER BY p.fecha DESC
                """;

        List<Pedido> lista = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new Pedido(
                        rs.getInt("id_pedido"),
                        rs.getString("fecha"),
                        rs.getDouble("total"),
                        rs.getString("estatus_actual"),
                        rs.getInt("id_cliente"),
                        rs.getString("nombre_cliente"),
                        rs.getInt("id_empleado"),
                        rs.getString("nombre_empleado")
                ));
            }
        }
        return lista;
    }

    // ── DETALLES DE UN PEDIDO ─────────────────────────────────
    public List<DetallePedido> obtenerDetalles(int idPedido) throws SQLException {
        String sql = """
                SELECT d.id_detalle, d.id_pedido, d.id_producto,
                       p.nombre AS nombre_producto,
                       d.cantidad, d.precio_unitario
                FROM DETALLE_PEDIDO d
                JOIN PRODUCTO p ON p.id_producto = d.id_producto
                WHERE d.id_pedido = ?
                """;

        List<DetallePedido> lista = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPedido);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(new DetallePedido(
                        rs.getInt("id_detalle"),
                        rs.getInt("id_pedido"),
                        rs.getInt("id_producto"),
                        rs.getString("nombre_producto"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precio_unitario")
                ));
            }
        }
        return lista;
    }

    // ── INSERTAR PEDIDO + DETALLES (transacción) ──────────────
    public boolean insertar(Pedido p, List<DetallePedido> detalles) throws SQLException {
        String sqlPedido  = """
                INSERT INTO PEDIDO (fecha, total, estatus_actual, id_cliente, id_empleado)
                VALUES (NOW(), ?, 'En proceso', ?, ?)
                """;
        String sqlDetalle = """
                INSERT INTO DETALLE_PEDIDO (id_pedido, id_producto, cantidad, precio_unitario)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            int idGenerado;

            try (PreparedStatement stmt = conn.prepareStatement(
                    sqlPedido, PreparedStatement.RETURN_GENERATED_KEYS)) {

                stmt.setDouble(1, p.getTotal());
                stmt.setInt(2, p.getIdCliente());
                stmt.setInt(3, p.getIdEmpleado());
                stmt.executeUpdate();

                ResultSet keys = stmt.getGeneratedKeys();
                if (!keys.next()) { conn.rollback(); return false; }
                idGenerado = keys.getInt(1);
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlDetalle)) {
                for (DetallePedido d : detalles) {
                    stmt.setInt(1,    idGenerado);
                    stmt.setInt(2,    d.getIdProducto());
                    stmt.setInt(3,    d.getCantidad());
                    stmt.setDouble(4, d.getPrecioUnitario());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit();
            return true;
        }
    }

    // ── CAMBIAR ESTATUS ───────────────────────────────────────
    public boolean cambiarEstatus(int idPedido, String nuevoEstatus) throws SQLException {
        String sql = "UPDATE PEDIDO SET estatus_actual = ? WHERE id_pedido = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevoEstatus);
            stmt.setInt(2, idPedido);
            return stmt.executeUpdate() > 0;
        }
    }

    // ── ELIMINAR LÓGICO (cancela) ─────────────────────────────
    public boolean cancelar(int idPedido) throws SQLException {
        return cambiarEstatus(idPedido, "Cancelado");
    }

    // ── CLIENTES disponibles ──────────────────────────────────
    public List<String[]> obtenerClientes() throws SQLException {
        String sql = """
                SELECT id_usuario, CONCAT(nombre, ' ', apellidos)
                FROM USUARIO
                WHERE tipo_usuario = 'Cliente' AND estatus = 'Activo'
                """;
        return obtenerPares(sql);
    }

    // ── EMPLEADOS disponibles ─────────────────────────────────
    public List<String[]> obtenerEmpleados() throws SQLException {
        String sql = """
                SELECT id_usuario, CONCAT(nombre, ' ', apellidos)
                FROM USUARIO
                WHERE tipo_usuario = 'Empleado' AND estatus = 'Activo'
                """;
        return obtenerPares(sql);
    }

    private List<String[]> obtenerPares(String sql) throws SQLException {
        List<String[]> lista = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next())
                lista.add(new String[]{ rs.getString(1), rs.getString(2) });
        }
        return lista;
    }
}