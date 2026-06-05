package com.italia.pizza.controllers;

import com.italia.pizza.dao.PedidoDAO;
import com.italia.pizza.models.Pedido;
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

public class PedidosController {

    @FXML private Label               lblUsuario;
    @FXML private TextField           txtBuscar;
    @FXML private ComboBox<String>    comboEstatus;
    @FXML private TableView<Pedido>   tablaPedidos;

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private ObservableList<Pedido> listaPedidos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        cargarColumnas();
        cargarFiltroEstatus();
        cargarPedidos();
        txtBuscar.textProperty().addListener((obs, old, nuevo) -> filtrar());
        comboEstatus.valueProperty().addListener((obs, old, nuevo) -> filtrar());

        // Cargar nombre y rol desde la sesión global
        if (Session.getNombreCompleto() != null) {
            lblUsuario.setText(Session.getNombreCompleto() + " (" + Session.getRol() + ")");
        }
    }

    private void cargarColumnas() {
        TableColumn<Pedido, Integer> colId = new TableColumn<>("#");
        colId.setCellValueFactory(c -> c.getValue().idPedidoProperty().asObject());
        colId.setPrefWidth(50);

        TableColumn<Pedido, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c -> c.getValue().fechaProperty());
        colFecha.setPrefWidth(160);

        TableColumn<Pedido, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(c -> c.getValue().nombreClienteProperty());
        colCliente.setPrefWidth(180);

        TableColumn<Pedido, String> colEmpleado = new TableColumn<>("Empleado");
        colEmpleado.setCellValueFactory(c -> c.getValue().nombreEmpleadoProperty());
        colEmpleado.setPrefWidth(180);

        TableColumn<Pedido, Double> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(c -> c.getValue().totalProperty().asObject());
        colTotal.setPrefWidth(100);

        TableColumn<Pedido, String> colEstatus = new TableColumn<>("Estatus");
        colEstatus.setCellValueFactory(c -> c.getValue().estatusProperty());
        colEstatus.setPrefWidth(120);

        tablaPedidos.getColumns().setAll(colId, colFecha, colCliente, colEmpleado, colTotal, colEstatus);
    }

    private void cargarFiltroEstatus() {
        comboEstatus.getItems().addAll("Todos", "En proceso", "Entregado", "Cancelado");
        comboEstatus.setValue("Todos");
    }

    private void cargarPedidos() {
        listaPedidos.clear();
        try {
            listaPedidos.addAll(pedidoDAO.obtenerTodos());
            tablaPedidos.setItems(listaPedidos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void filtrar() {
        String texto  = txtBuscar.getText().trim().toLowerCase();
        String estado = comboEstatus.getValue();

        ObservableList<Pedido> filtrados = FXCollections.observableArrayList();
        for (Pedido p : listaPedidos) {
            boolean coincideTexto =
                    p.getNombreCliente().toLowerCase().contains(texto) ||
                    p.getNombreEmpleado().toLowerCase().contains(texto) ||
                    String.valueOf(p.getIdPedido()).contains(texto);

            boolean coincideEstatus =
                    "Todos".equals(estado) || p.getEstatus().equals(estado);

            if (coincideTexto && coincideEstatus) filtrados.add(p);
        }
        tablaPedidos.setItems(filtrados);
    }

    @FXML
    private void registrarPedido() { abrirFormulario(null); }

    @FXML
    private void editarPedido() {
        Pedido sel = tablaPedidos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alerta("Selecciona un pedido para editar.", Alert.AlertType.WARNING);
            return;
        }
        if ("Cancelado".equals(sel.getEstatus())) {
            alerta("No se puede editar un pedido cancelado.", Alert.AlertType.WARNING);
            return;
        }
        abrirFormulario(sel);
    }

    @FXML
    private void cambiarEstatus() {
        Pedido sel = tablaPedidos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alerta("Selecciona un pedido para cambiar su estatus.", Alert.AlertType.WARNING);
            return;
        }
        if ("Cancelado".equals(sel.getEstatus())) {
            alerta("El pedido ya está cancelado.", Alert.AlertType.WARNING);
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(sel.getEstatus(),
                "En proceso", "Entregado", "Cancelado");
        dialog.setTitle("Cambiar estatus");
        dialog.setHeaderText("Pedido #" + sel.getIdPedido());
        dialog.setContentText("Selecciona el nuevo estatus:");

        dialog.showAndWait().ifPresent(nuevoEstatus -> {
            try {
                if (pedidoDAO.cambiarEstatus(sel.getIdPedido(), nuevoEstatus)) {
                    sel.setEstatus(nuevoEstatus);
                    tablaPedidos.refresh();
                    alerta("Estatus actualizado correctamente.", Alert.AlertType.INFORMATION);
                }
            } catch (Exception e) {
                alerta("Error: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void eliminarPedido() {
        Pedido sel = tablaPedidos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alerta("Selecciona un pedido para cancelar.", Alert.AlertType.WARNING);
            return;
        }
        if ("Cancelado".equals(sel.getEstatus())) {
            alerta("El pedido ya está cancelado.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("¿Cancelar pedido #" + sel.getIdPedido() + "?");
        confirm.setContentText("El pedido será marcado como Cancelado.");

        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    if (pedidoDAO.cancelar(sel.getIdPedido())) {
                        sel.setEstatus("Cancelado");
                        tablaPedidos.refresh();
                        alerta("Pedido cancelado.", Alert.AlertType.INFORMATION);
                    }
                } catch (Exception e) {
                    alerta("Error: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void abrirFormulario(Pedido pedido) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/PedidosForm.fxml"));
            Parent root = loader.load();

            PedidosFormController ctrl = loader.getController();
            if (pedido == null) ctrl.modoRegistro();
            else                ctrl.cargarPedido(pedido);

            Stage dialog = new Stage();
            dialog.setTitle(pedido == null ? "Registrar pedido" : "Editar pedido");
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(lblUsuario.getScene().getWindow());
            dialog.showAndWait();

            cargarPedidos();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void alerta(String msg, Alert.AlertType tipo) {
        Alert a = new Alert(tipo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // ---------------------------------------------------------
    // NAVEGACIÓN
    // ---------------------------------------------------------
    
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
        Session.cerrar();
        cambiarVista("/fxml/login.fxml");
    }

    public void setUsuario(String nombre) {
        lblUsuario.setText(nombre);
    }
}