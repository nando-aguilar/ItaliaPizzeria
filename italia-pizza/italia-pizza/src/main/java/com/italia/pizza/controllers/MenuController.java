package com.italia.pizza.controllers;

import com.italia.pizza.config.DBConnection;
import com.italia.pizza.models.MenuTablas;
import com.italia.pizza.utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;

public class MenuController {

    @FXML private Label lblUsuario;
    @FXML private Label lblAdministracion;
    @FXML private Button btnUsuarios, btnProductos, btnValidacion, btnPedidos, btnAyuda;

    @FXML private TableView<MenuTablas.ProductoVista> tablaVistaProductos;
    @FXML private TableView<MenuTablas.PedidoVista>   tablaVistaPedidos;
    @FXML private TableView<MenuTablas.DetalleVista>  tablaVistaDetalles;
    @FXML private TableView<MenuTablas.UsuarioReciente> tablaVistaUsuariosRecientes;

    private String rolUsuario;

    @FXML
    private void initialize() {
        configurarColumnas();
        cargarDatosVistas();
        
        if (Session.getNombreCompleto() != null) {
            this.rolUsuario = Session.getRol();
            lblUsuario.setText(Session.getNombreCompleto() + " (" + Session.getRol() + ")");
            aplicarPermisos();
        }
    }

    private void configurarColumnas() {
        
        configurarTabla(tablaVistaProductos, "ID", "id", "Nombre", "nombre", "Código", "codigo", "Precio", "precio", "Cantidad", "cantidad", "Estatus", "estatus");
        
        configurarTabla(tablaVistaPedidos, "ID", "id", "Fecha", "fecha", "Total", "total", "Estatus", "estatus", "Cliente", "cliente", "Empleado", "empleado");
        
        configurarTabla(tablaVistaDetalles, "ID", "id", "ID Pedido", "idPedido", "Producto", "producto", "Cantidad", "cantidad", "Precio U.", "precio", "Subtotal", "subtotal");
        
        configurarTabla(tablaVistaUsuariosRecientes, "ID", "id", "Nombre Completo", "nombre", "Tipo", "tipo", "Estatus", "estatus");
    }
    
    public void setUsuarioLogueado(String nombreCompleto, String rol) {
        this.rolUsuario = rol;
        lblUsuario.setText(nombreCompleto + " (" + rol + ")");
        aplicarPermisos();
    }

    private void configurarTabla(TableView<?> tabla, String... titulosYCampos) {
        for (int i = 0; i < titulosYCampos.length; i += 2) {
            String titulo = titulosYCampos[i];
            String campo = titulosYCampos[i+1];
            TableColumn<Object, String> col = new TableColumn<>(titulo);
            col.setCellValueFactory(data -> {
                try {
                    Object obj = data.getValue();
                    java.lang.reflect.Field field = obj.getClass().getField(campo);
                    return (javafx.beans.property.SimpleStringProperty) field.get(obj);
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleStringProperty("");
                }
            });
            ((TableView<Object>) tabla).getColumns().add(col);
        }
    }

    private void cargarDatosVistas() {
        try (Connection conn = DBConnection.getConnection()) {
            
            // 1. Productos
            ObservableList<MenuTablas.ProductoVista> prodList = FXCollections.observableArrayList();
            try (ResultSet rs = conn.prepareStatement("SELECT * FROM vw_productos_inventario").executeQuery()) {
                while (rs.next()) {
                    prodList.add(new MenuTablas.ProductoVista(rs.getString("id_producto"), rs.getString("nombre"), rs.getString("codigo"), rs.getString("precio"), rs.getString("cantidad"), rs.getString("estatus")));
                }
            }
            tablaVistaProductos.setItems(prodList);

            // 2. Pedidos
            ObservableList<MenuTablas.PedidoVista> pedList = FXCollections.observableArrayList();
            try (ResultSet rs = conn.prepareStatement("SELECT * FROM vw_pedidos_detalle").executeQuery()) {
                while (rs.next()) {
                    pedList.add(new MenuTablas.PedidoVista(rs.getString("id_pedido"), rs.getString("fecha"), rs.getString("total"), rs.getString("estatus_actual"), rs.getString("cliente_nombre") + " " + rs.getString("cliente_apellidos"), rs.getString("empleado_nombre") + " " + rs.getString("empleado_apellidos")));
                }
            }
            tablaVistaPedidos.setItems(pedList);

            // 3. Detalles
            ObservableList<MenuTablas.DetalleVista> detList = FXCollections.observableArrayList();
            try (ResultSet rs = conn.prepareStatement("SELECT * FROM vw_detalle_pedido_completo").executeQuery()) {
                while (rs.next()) {
                    detList.add(new MenuTablas.DetalleVista(rs.getString("id_detalle"), rs.getString("id_pedido"), rs.getString("producto"), rs.getString("cantidad"), rs.getString("precio_unitario"), rs.getString("subtotal")));
                }
            }
            tablaVistaDetalles.setItems(detList);
            
            // 4. Usuarios Recientes
            ObservableList<MenuTablas.UsuarioReciente> usrList = FXCollections.observableArrayList();
            try (ResultSet rs = conn.prepareStatement("SELECT * FROM vw_usuarios_recientes").executeQuery()) {
                while (rs.next()) {
                    usrList.add(new MenuTablas.UsuarioReciente(rs.getString("id"), rs.getString("nombre_completo"), rs.getString("tipo"), rs.getString("estatus")));
                }
            }
            tablaVistaUsuariosRecientes.setItems(usrList);

        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void volverAlMenu(MouseEvent event) { cargarDatosVistas(); }
    @FXML private void openUsuarios() { cambiarVista("/fxml/usuarios.fxml"); }
    @FXML private void openProductos() { cambiarVista("/fxml/productos.fxml"); }
    @FXML private void openValidacion() { cambiarVista("/fxml/validacion.fxml"); }
    @FXML private void openPedidos() { cambiarVista("/fxml/Pedidos.fxml"); }
    @FXML private void openAyuda() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText("Sistema Italia Pizzería");
        alert.setContentText(com.italia.pizza.utils.Constantes.TEXTO_ACERCA_DE);
        alert.showAndWait();
    }

    private void cambiarVista(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            Parent root = loader.load();
            Stage stage = (Stage) lblUsuario.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void logout() {
        try {
            Session.cerrar();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblUsuario.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void aplicarPermisos() {
        if (rolUsuario == null) return;
        
        if ("CAJERO".equals(rolUsuario)) {
            if (lblAdministracion != null) {
                lblAdministracion.setVisible(false);
                lblAdministracion.setManaged(false);
            }
            if (btnUsuarios != null) {
                btnUsuarios.setVisible(false);
                btnUsuarios.setManaged(false);
            }
        }
    }
}