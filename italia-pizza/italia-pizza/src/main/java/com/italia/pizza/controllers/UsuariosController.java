package com.italia.pizza.controllers;

import com.italia.pizza.config.DBConnection;
import com.italia.pizza.dao.UsuarioDAO;
import com.italia.pizza.models.Usuario;
import com.italia.pizza.utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.italia.pizza.utils.Constantes;

public class UsuariosController {

    @FXML private Label              lblUsuario;
    @FXML private TextField          txtBuscar;
    @FXML private ComboBox<String>   comboFiltro;
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private Button             btnNuevoUsuario;
    @FXML private Button             btnEditarUsuario;
    @FXML private Button             btnEliminarUsuario;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private ObservableList<Usuario> listaUsuarios = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        cargarColumnas();
        cargarFiltro();
        cargarUsuarios();

        txtBuscar.textProperty().addListener((obs, oldValue, newValue) -> buscarUsuario());
        comboFiltro.valueProperty().addListener((obs, oldValue, newValue) -> buscarUsuario());

        if (Session.getNombreCompleto() != null) {
            lblUsuario.setText(Session.getNombreCompleto() + " (" + Session.getRol() + ")");
        }
        aplicarPermisos();
    }

    private void aplicarPermisos() {
        if ("CAJERO".equals(Session.getRol())) {
            if (btnNuevoUsuario  != null) btnNuevoUsuario.setVisible(false);
            if (btnEditarUsuario != null) btnEditarUsuario.setVisible(false);
            if (btnEliminarUsuario != null) btnEliminarUsuario.setVisible(false);
        }
    }

    private void cargarColumnas() {
        TableColumn<Usuario, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(cell -> cell.getValue().nombreProperty());

        TableColumn<Usuario, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(cell -> cell.getValue().telefonoProperty());

        TableColumn<Usuario, String> colEmail = new TableColumn<>("Correo");
        colEmail.setCellValueFactory(cell -> cell.getValue().emailProperty());

        TableColumn<Usuario, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(cell -> cell.getValue().tipoProperty());

        TableColumn<Usuario, String> colEstatus = new TableColumn<>("Estatus");
        colEstatus.setCellValueFactory(cell -> cell.getValue().estatusProperty());

        tablaUsuarios.getColumns().setAll(colNombre, colTelefono, colEmail, colTipo, colEstatus);
    }

    private void cargarFiltro() {
        comboFiltro.getItems().addAll("Todos", "Empleado", "Cliente");
        comboFiltro.setValue("Todos");
    }

    private void cargarUsuarios() {
        listaUsuarios.clear();

        String sql = """
                SELECT id_usuario, nombre, apellidos, telefono, email, tipo_usuario, estatus
                FROM USUARIO
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                listaUsuarios.add(new Usuario(
                        rs.getInt("id_usuario"),
                        rs.getString("nombre") + " " + rs.getString("apellidos"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getString("tipo_usuario"),
                        rs.getString("estatus")
                ));
            }

            tablaUsuarios.setItems(listaUsuarios);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void buscarUsuario() {
        String filtro = txtBuscar.getText().trim().toLowerCase();
        String tipo   = comboFiltro.getValue();

        ObservableList<Usuario> filtrados = FXCollections.observableArrayList();

        for (Usuario u : listaUsuarios) {
            boolean coincideTexto =
                    u.getNombre().toLowerCase().contains(filtro) ||
                    u.getTelefono().toLowerCase().contains(filtro) ||
                    u.getEmail().toLowerCase().contains(filtro) ||
                    u.getTipo().toLowerCase().contains(filtro);

            boolean coincideTipo =
                    tipo.equals("Todos") || u.getTipo().equalsIgnoreCase(tipo);

            if (coincideTexto && coincideTipo) {
                filtrados.add(u);
            }
        }

        tablaUsuarios.setItems(filtrados);
    }

    @FXML
    private void registrarUsuario() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UsuariosForm.fxml"));
            Parent root = loader.load();

            UsuariosFormController controller = loader.getController();
            controller.modoRegistro();

            Stage dialog = new Stage();
            dialog.setTitle("Registrar usuario");
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(lblUsuario.getScene().getWindow());
            dialog.showAndWait();

            cargarUsuarios();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void editarUsuario() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona un usuario de la tabla para editarlo.").showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UsuariosForm.fxml"));
            Parent root = loader.load();

            UsuariosFormController controller = loader.getController();
            controller.cargarUsuario(seleccionado);

            Stage stage = new Stage();
            stage.setTitle("Editar usuario");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(lblUsuario.getScene().getWindow());
            stage.showAndWait();

            cargarUsuarios();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void eliminarUsuario() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona un usuario para eliminarlo.").showAndWait();
            return;
        }

        try {
            if (usuarioDAO.tienePedidos(seleccionado.getIdUsuario())) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText("No se puede eliminar");
                alert.setContentText("El usuario tiene pedidos asociados y no puede ser dado de baja.");
                alert.showAndWait();
                return;
            }

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setHeaderText("¿Dar de baja a " + seleccionado.getNombre() + "?");
            confirmacion.setContentText("El usuario será marcado como inactivo.");

            confirmacion.showAndWait().ifPresent(respuesta -> {
                if (respuesta == ButtonType.OK) {
                    try {
                        boolean desactivado = usuarioDAO.desactivarUsuario(seleccionado.getIdUsuario());
                        if (desactivado) {
                            seleccionado.setEstatus("Inactivo");
                            tablaUsuarios.refresh();
                            new Alert(Alert.AlertType.INFORMATION, "Usuario dado de baja correctamente.").showAndWait();
                        }
                    } catch (Exception e) {
                        new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).showAndWait();
                    }
                }
            });

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).showAndWait();
        }
    }

    
   @FXML
    private void volverAlMenu(MouseEvent event) {
        cambiarVista("/fxml/menu.fxml"); 
    }

    @FXML
    private void openUsuarios() {
        cambiarVista("/fxml/usuarios.fxml");
    }

    @FXML
    private void openProductos() {
        cambiarVista("/fxml/productos.fxml");
    }

    @FXML
    private void openValidacion() {
        cambiarVista("/fxml/validacion.fxml");
    }

    @FXML
    private void openPedidos() {
        cambiarVista("/fxml/Pedidos.fxml");
    }

    @FXML
    private void openAyuda() {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        try {
            Session.cerrar();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblUsuario.getScene().getWindow();
            stage.setTitle("Italia Pizza - Login");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUsuario(String nombre) {
        lblUsuario.setText(nombre);
    }
}