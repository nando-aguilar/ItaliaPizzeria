/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.italia.pizza.dao;

import com.italia.pizza.config.DBConnection;
import com.italia.pizza.models.Usuario;
import com.italia.pizza.models.UsuarioCompleto;
import com.italia.pizza.models.UsuarioLogin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 *
 * @author brian
 */
public class UsuarioDAO {

    private static final String SQL_BUSCAR_USUARIO = """
            SELECT ul.rol, u.nombre, u.apellidos
            FROM usuarios_login ul
            INNER JOIN USUARIO u ON u.id_usuario = ul.id_usuario
            WHERE ul.usuario = ? AND ul.password = ?
            """;


    public Optional<UsuarioLogin> buscarPorCredenciales(String usuario, String password) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = prepararConsulta(conn, usuario, password);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return Optional.of(mapearUsuario(rs));
            }

            return Optional.empty();
        }
    }

    private PreparedStatement prepararConsulta(Connection conn, String usuario, String password)
            throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(SQL_BUSCAR_USUARIO);
        stmt.setString(1, usuario);
        stmt.setString(2, password);
        return stmt;
    }

    private UsuarioLogin mapearUsuario(ResultSet rs) throws SQLException {
        String nombre    = rs.getString("nombre");
        String apellidos = rs.getString("apellidos");
        String rol       = rs.getString("rol");
        return new UsuarioLogin(nombre, apellidos, rol);
    }

    public boolean desactivarUsuario(int idUsuario) throws SQLException {
        String sql = "UPDATE USUARIO SET estatus = 'Inactivo' WHERE id_usuario = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminarFisico(int idUsuario) throws SQLException {
        String sql = "DELETE FROM USUARIO WHERE id_usuario = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean tienePedidos(int idUsuario) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pedido WHERE id_cliente = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    public Optional<UsuarioCompleto> buscarPorId(int idUsuario) throws SQLException {
        String sql = """
            SELECT id_usuario, nombre, apellidos, telefono, email,
                   calle_numero, codigo_postal, ciudad, tipo_usuario,
                   nombre_usuario, contrasena, estatus
            FROM USUARIO
            WHERE id_usuario = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(new UsuarioCompleto(
                        rs.getInt("id_usuario"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getString("calle_numero"),
                        rs.getString("codigo_postal"),
                        rs.getString("ciudad"),
                        rs.getString("tipo_usuario"),
                        rs.getString("nombre_usuario"),
                        rs.getString("contrasena"),
                        rs.getString("estatus")
                ));
            }

            return Optional.empty();
        }
    }

    public boolean actualizarUsuario(UsuarioCompleto u) throws SQLException {
        String sql = """
        UPDATE USUARIO
        SET nombre = ?, apellidos = ?, telefono = ?, email = ?,
            calle_numero = ?, codigo_postal = ?, ciudad = ?,
            tipo_usuario = ?, nombre_usuario = ?, contrasena = ?
        WHERE id_usuario = ?
        """;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, u.getNombreSolo());
                stmt.setString(2, u.getApellidos());
                stmt.setString(3, u.getTelefono());
                stmt.setString(4, u.getEmail());
                stmt.setString(5, u.getCalleNumero());
                stmt.setString(6, u.getCodigoPostal());
                stmt.setString(7, u.getCiudad());
                stmt.setString(8, u.getTipo());
                stmt.setString(9, u.getNombreUsuario());
                stmt.setString(10, u.getContrasena());
                stmt.setInt(11, u.getIdUsuario());
                stmt.executeUpdate();
            }

            if ("Empleado".equals(u.getTipo())) {
                actualizarLogin(conn, u);
            }

            conn.commit();
            return true;

        }
    }

    private void actualizarLogin(Connection conn, UsuarioCompleto u) throws SQLException {
        String sql = """
        UPDATE usuarios_login
        SET usuario = ?, password = ?
        WHERE id_usuario = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u.getNombreUsuario());
            stmt.setString(2, u.getContrasena());
            stmt.setInt(3, u.getIdUsuario());
            stmt.executeUpdate();
        }
    }
    public boolean insertarUsuario(UsuarioCompleto u) throws SQLException {
    String sqlUsuario = """
        INSERT INTO USUARIO (nombre, apellidos, telefono, email,
                             calle_numero, codigo_postal, ciudad,
                             tipo_usuario, nombre_usuario, contrasena, estatus)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    try (Connection conn = DBConnection.getConnection()) {
        conn.setAutoCommit(false);

        int idGenerado;

        try (PreparedStatement stmt = conn.prepareStatement(
                sqlUsuario, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1,  u.getNombreSolo());
            stmt.setString(2,  u.getApellidos());
            stmt.setString(3,  u.getTelefono());
            stmt.setString(4,  u.getEmail());
            stmt.setString(5,  u.getCalleNumero());
            stmt.setString(6,  u.getCodigoPostal());
            stmt.setString(7,  u.getCiudad());
            stmt.setString(8,  u.getTipo());
            stmt.setString(9,  u.getNombreUsuario());
            stmt.setString(10, u.getContrasena());
            stmt.setString(11, u.getEstatus());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (!keys.next()) {
                conn.rollback();
                return false;
            }
            idGenerado = keys.getInt(1);
        }

        // Si es empleado, insertar también en usuarios_login
        if ("Empleado".equals(u.getTipo())) {
            insertarLogin(conn, idGenerado, u);
        }

        conn.commit();
        return true;

    }
}

private void insertarLogin(Connection conn, int idUsuario, UsuarioCompleto u) throws SQLException {
    String sql = """
        INSERT INTO usuarios_login (id_usuario, usuario, password, rol)
        VALUES (?, ?, ?, 'Empleado')
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1,    idUsuario);
        stmt.setString(2, u.getNombreUsuario());
        stmt.setString(3, u.getContrasena());
        stmt.executeUpdate();
    }
}

}
