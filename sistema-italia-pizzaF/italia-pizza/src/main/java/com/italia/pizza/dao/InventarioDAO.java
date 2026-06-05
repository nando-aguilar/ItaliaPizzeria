package com.italia.pizza.dao;
 
import com.italia.pizza.config.DBConnection;
import com.italia.pizza.models.ValidacionItem;
 
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
 
public class InventarioDAO {
 
    public List<ValidacionItem> obtenerProductosActivos() throws SQLException {
        String sql = """
                SELECT id_producto, nombre, cantidad
                FROM PRODUCTO
                WHERE estatus = 'Activo'
                ORDER BY nombre
                """;
 
        List<ValidacionItem> lista = new ArrayList<>();
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
 
            while (rs.next()) {
                lista.add(new ValidacionItem(
                        rs.getInt("id_producto"),
                        rs.getString("nombre"),
                        rs.getInt("cantidad")
                ));
            }
        }
        return lista;
    }
 
    /**
     * Solo actualiza los productos que el usuario capturó (isCapturado == true).
     * Los que no se tocaron conservan su cantidad original en BD.
     */
    public int guardarValidacion(List<ValidacionItem> items) throws SQLException {
        String sql = "UPDATE PRODUCTO SET cantidad = ? WHERE id_producto = ?";
        int actualizados = 0;
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            for (ValidacionItem item : items) {
                if (!item.isCapturado()) continue; // ← solo los capturados
 
                stmt.setInt(1, item.getEnFisico());
                stmt.setInt(2, item.getIdProducto());
                stmt.addBatch();
                actualizados++;
            }
 
            if (actualizados > 0) stmt.executeBatch();
        }
        return actualizados;
    }
}