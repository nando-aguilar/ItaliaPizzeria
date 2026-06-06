package com.italia.pizza.controllers;

import com.italia.pizza.dao.ProductoDAO;
import com.italia.pizza.models.Producto;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ProductosFormController {

    @FXML private Label     lblTitulo;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextArea  txtDescripcion;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtRestricciones;

    private Producto productoEditando = null;
    private final ProductoDAO productoDAO = new ProductoDAO();

    public void modoRegistro() {
        this.productoEditando = null;
        lblTitulo.setText("Registrar Producto");
    }

    public void cargarProducto(Producto p) {
        this.productoEditando = p;
        lblTitulo.setText("Editar Producto");
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion());
        txtPrecio.setText(String.valueOf(p.getPrecio()));
        txtCantidad.setText(String.valueOf(p.getCantidad()));
        txtRestricciones.setText(p.getRestricciones());
    }

    @FXML
    private void guardar() {
        if (!validar()) return;

        try {
            String codigo        = txtCodigo.getText().trim();
            String nombre        = txtNombre.getText().trim();
            String descripcion   = txtDescripcion.getText().trim();
            double precio        = Double.parseDouble(txtPrecio.getText().trim());
            int    cantidad      = Integer.parseInt(txtCantidad.getText().trim());
            String restricciones = txtRestricciones.getText().trim();

            if (productoEditando == null) {
                Producto nuevo = new Producto(
                        0, codigo, nombre, descripcion,
                        precio, restricciones, cantidad, "Activo");
                if (productoDAO.insertar(nuevo)) {
                    alerta("Producto registrado correctamente.", Alert.AlertType.INFORMATION);
                    cerrar();
                } else {
                    alerta("No se pudo registrar el producto.", Alert.AlertType.ERROR);
                }
            } else {
                productoEditando.setCodigo(codigo);
                productoEditando.setNombre(nombre);
                productoEditando.setDescripcion(descripcion);
                productoEditando.setPrecio(precio);
                productoEditando.setCantidad(cantidad);
                productoEditando.setRestricciones(restricciones);
                if (productoDAO.actualizar(productoEditando)) {
                    alerta("Producto actualizado correctamente.", Alert.AlertType.INFORMATION);
                    cerrar();
                } else {
                    alerta("No se pudo actualizar el producto.", Alert.AlertType.ERROR);
                }
            }

        } catch (NumberFormatException e) {
            alerta("Precio y cantidad deben ser números válidos.", Alert.AlertType.WARNING);
        } catch (Exception e) {
            alerta("Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validar() {
        if (txtCodigo.getText().isBlank()      ||
            txtNombre.getText().isBlank()      ||
            txtDescripcion.getText().isBlank() ||
            txtPrecio.getText().isBlank()      ||
            txtCantidad.getText().isBlank()) {
            alerta("Los campos marcados con * son obligatorios.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    @FXML
    private void cancelar() { cerrar(); }

    private void cerrar() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    private void alerta(String msg, Alert.AlertType tipo) {
        Alert a = new Alert(tipo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}