package com.italia.pizza.controllers;

import com.italia.pizza.dao.ProductoDAO;
import com.italia.pizza.models.Producto;
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

public class ProductosController {

    @FXML private Label               lblUsuario;
    @FXML private TextField           txtBuscar;
    @FXML private TableView<Producto> tablaProductos;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        cargarColumnas();
        cargarProductos();
        txtBuscar.textProperty().addListener((obs, old, nuevo) -> buscarProducto());

        // Cargar nombre y rol desde la sesión global
        if (Session.getNombreCompleto() != null) {
            lblUsuario.setText(Session.getNombreCompleto() + " (" + Session.getRol() + ")");
        }
    }

    private void cargarColumnas() {
        TableColumn<Producto, String>  colCodigo      = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(cell -> cell.getValue().codigoProperty());

        TableColumn<Producto, String>  colNombre      = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(cell -> cell.getValue().nombreProperty());

        TableColumn<Producto, String>  colDescripcion = new TableColumn<>("Descripción");
        colDescripcion.setCellValueFactory(cell -> cell.getValue().descripcionProperty());

        TableColumn<Producto, Double>  colPrecio      = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(cell -> cell.getValue().precioProperty().asObject());

        TableColumn<Producto, Integer> colCantidad    = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(cell -> cell.getValue().cantidadProperty().asObject());

        TableColumn<Producto, String>  colEstatus     = new TableColumn<>("Estatus");
        colEstatus.setCellValueFactory(cell -> cell.getValue().estatusProperty());

        tablaProductos.getColumns().setAll(
                colCodigo, colNombre, colDescripcion, colPrecio, colCantidad, colEstatus);
    }

    private void cargarProductos() {
        listaProductos.clear();
        try {
            listaProductos.addAll(productoDAO.obtenerTodos());
            tablaProductos.setItems(listaProductos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void buscarProducto() {
        String filtro = txtBuscar.getText().trim().toLowerCase();
        if (filtro.isEmpty()) {
            tablaProductos.setItems(listaProductos);
            return;
        }
        ObservableList<Producto> filtrados = FXCollections.observableArrayList();
        for (Producto p : listaProductos) {
            if (p.getNombre().toLowerCase().contains(filtro) ||
                p.getCodigo().toLowerCase().contains(filtro))
                filtrados.add(p);
        }
        tablaProductos.setItems(filtrados);
    }

    @FXML
    private void registrarProducto() { abrirFormulario(null); }

    @FXML
    private void editarProducto() {
        Producto sel = tablaProductos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona un producto para editar.").showAndWait();
            return;
        }
        abrirFormulario(sel);
    }

    @FXML
    private void eliminarProducto() {
        Producto sel = tablaProductos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona un producto para eliminar.").showAndWait();
            return;
        }

        try {
            if (productoDAO.tieneVentas(sel.getIdProducto())) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setHeaderText("No se puede eliminar");
                a.setContentText("El producto tiene ventas asociadas y no puede darse de baja.");
                a.showAndWait();
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setHeaderText("¿Dar de baja a " + sel.getNombre() + "?");
            confirm.setContentText("El producto será marcado como Inactivo.");

            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    try {
                        if (productoDAO.desactivar(sel.getIdProducto())) {
                            sel.setEstatus("Inactivo");
                            tablaProductos.refresh();
                            new Alert(Alert.AlertType.INFORMATION,
                                    "Producto dado de baja.").showAndWait();
                        }
                    } catch (Exception e) {
                        new Alert(Alert.AlertType.ERROR,
                                "Error: " + e.getMessage()).showAndWait();
                    }
                }
            });

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).showAndWait();
        }
    }

    private void abrirFormulario(Producto producto) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/ProductosForm.fxml"));
            Parent root = loader.load();

            ProductosFormController ctrl = loader.getController();
            if (producto == null) ctrl.modoRegistro();
            else                  ctrl.cargarProducto(producto);

            Stage dialog = new Stage();
            dialog.setTitle(producto == null ? "Registrar producto" : "Editar producto");
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(lblUsuario.getScene().getWindow());
            dialog.showAndWait();

            cargarProductos();

        } catch (Exception e) {
            e.printStackTrace();
        }
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