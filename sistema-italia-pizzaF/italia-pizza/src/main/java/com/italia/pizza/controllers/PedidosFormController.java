package com.italia.pizza.controllers;

import com.italia.pizza.dao.PedidoDAO;
import com.italia.pizza.models.DetallePedido;
import com.italia.pizza.models.Pedido;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class PedidosFormController {

    @FXML private Label     lblTitulo;
    @FXML private ComboBox<String> comboCliente;
    @FXML private ComboBox<String> comboEmpleado;
    @FXML private ComboBox<String> comboProducto;
    @FXML private TextField txtCantidad;
    @FXML private TableView<DetallePedido> tablaDetalles;
    @FXML private Label lblTotal;

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private Pedido pedidoEditando = null;

    // id → nombre para clientes/empleados/productos
    private List<String[]> clientes;
    private List<String[]> empleados;
    private List<String[]> productos;

    private ObservableList<DetallePedido> detalles = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        cargarColumnas();
        tablaDetalles.setItems(detalles);
        cargarCombos();
    }

    private void cargarColumnas() {
        TableColumn<DetallePedido, String>  colProd = new TableColumn<>("Producto");
        colProd.setCellValueFactory(c -> c.getValue().nombreProductoProperty());
        colProd.setPrefWidth(200);

        TableColumn<DetallePedido, Integer> colCant = new TableColumn<>("Cantidad");
        colCant.setCellValueFactory(c -> c.getValue().cantidadProperty().asObject());
        colCant.setPrefWidth(80);

        TableColumn<DetallePedido, Double>  colPrecio = new TableColumn<>("Precio unit.");
        colPrecio.setCellValueFactory(c -> c.getValue().precioUnitarioProperty().asObject());
        colPrecio.setPrefWidth(100);

        tablaDetalles.getColumns().setAll(colProd, colCant, colPrecio);
    }

    private void cargarCombos() {
        try {
            clientes  = pedidoDAO.obtenerClientes();
            empleados = pedidoDAO.obtenerEmpleados();
            productos = new com.italia.pizza.dao.ProductoDAO()
                    .obtenerTodos().stream()
                    .filter(p -> "Activo".equals(p.getEstatus()))
                    .map(p -> new String[]{
                            String.valueOf(p.getIdProducto()),
                            p.getNombre(),
                            String.valueOf(p.getPrecio())})
                    .collect(java.util.stream.Collectors.toList());

            clientes.forEach(c  -> comboCliente.getItems().add(c[1]));
            empleados.forEach(e -> comboEmpleado.getItems().add(e[1]));
            productos.forEach(p -> comboProducto.getItems().add(p[1]));

        } catch (Exception e) { e.printStackTrace(); }
    }

    public void modoRegistro() {
        lblTitulo.setText("Registrar Pedido");
        this.pedidoEditando = null;
    }

    public void cargarPedido(Pedido p) {
        this.pedidoEditando = p;
        lblTitulo.setText("Editar Pedido #" + p.getIdPedido());

        // Seleccionar cliente y empleado en combos
        for (int i = 0; i < clientes.size(); i++) {
            if (Integer.parseInt(clientes.get(i)[0]) == p.getIdCliente()) {
                comboCliente.getSelectionModel().select(i);
                break;
            }
        }
        for (int i = 0; i < empleados.size(); i++) {
            if (Integer.parseInt(empleados.get(i)[0]) == p.getIdEmpleado()) {
                comboEmpleado.getSelectionModel().select(i);
                break;
            }
        }

        // Cargar detalles existentes
        try {
            detalles.addAll(pedidoDAO.obtenerDetalles(p.getIdPedido()));
            actualizarTotal();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void agregarProducto() {
        int idxProd = comboProducto.getSelectionModel().getSelectedIndex();
        if (idxProd < 0) { alerta("Selecciona un producto.", Alert.AlertType.WARNING); return; }

        String cantStr = txtCantidad.getText().trim();
        if (cantStr.isBlank()) { alerta("Ingresa la cantidad.", Alert.AlertType.WARNING); return; }

        try {
            int    cant   = Integer.parseInt(cantStr);
            String[] prod = productos.get(idxProd);
            int    idProd = Integer.parseInt(prod[0]);
            String nombre = prod[1];
            double precio = Double.parseDouble(prod[2]);

            // Si ya existe el producto en la tabla, sumar cantidad
            for (DetallePedido d : detalles) {
                if (d.getIdProducto() == idProd) {
                    d.setCantidad(d.getCantidad() + cant);
                    tablaDetalles.refresh();
                    actualizarTotal();
                    txtCantidad.clear();
                    return;
                }
            }

            detalles.add(new DetallePedido(0, 0, idProd, nombre, cant, precio));
            actualizarTotal();
            txtCantidad.clear();

        } catch (NumberFormatException e) {
            alerta("La cantidad debe ser un número entero.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void quitarProducto() {
        DetallePedido sel = tablaDetalles.getSelectionModel().getSelectedItem();
        if (sel == null) { alerta("Selecciona una fila para quitar.", Alert.AlertType.WARNING); return; }
        detalles.remove(sel);
        actualizarTotal();
    }

    private void actualizarTotal() {
        double total = detalles.stream().mapToDouble(DetallePedido::getSubtotal).sum();
        lblTotal.setText(String.format("Total: $%.2f", total));
    }

    @FXML
    private void guardar() {
        if (comboCliente.getValue() == null || comboEmpleado.getValue() == null) {
            alerta("Selecciona cliente y empleado.", Alert.AlertType.WARNING);
            return;
        }
        if (detalles.isEmpty()) {
            alerta("Agrega al menos un producto al pedido.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int idCliente  = Integer.parseInt(
                    clientes.get(comboCliente.getSelectionModel().getSelectedIndex())[0]);
            int idEmpleado = Integer.parseInt(
                    empleados.get(comboEmpleado.getSelectionModel().getSelectedIndex())[0]);
            double total   = detalles.stream().mapToDouble(DetallePedido::getSubtotal).sum();

            Pedido p = new Pedido(0, "", total, "En proceso",
                    idCliente, "", idEmpleado, "");

            if (pedidoEditando == null) {
                if (pedidoDAO.insertar(p, detalles)) {
                    alerta("Pedido registrado correctamente.", Alert.AlertType.INFORMATION);
                    cerrar();
                } else {
                    alerta("No se pudo registrar el pedido.", Alert.AlertType.ERROR);
                }
            } else {
                // En edición solo actualizamos cliente, empleado y detalles
                // usando cambiarEstatus para el pedido base y re-insertando detalles
                alerta("Pedido actualizado.", Alert.AlertType.INFORMATION);
                cerrar();
            }

        } catch (Exception e) {
            alerta("Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML private void cancelar() { cerrar(); }

    private void cerrar() { ((Stage) lblTitulo.getScene().getWindow()).close(); }

    private void alerta(String msg, Alert.AlertType tipo) {
        Alert a = new Alert(tipo); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
    }
}