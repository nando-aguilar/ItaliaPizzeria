package com.italia.pizza.controllers;

import com.italia.pizza.dao.InventarioDAO;
import com.italia.pizza.models.ValidacionItem;
import com.italia.pizza.utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

public class ValidacionController {

    @FXML private Label                          lblUsuario;
    @FXML private TableView<ValidacionItem>      tablaValidacion;
    @FXML private Label                          lblResumen;

    // Inyecciones para aplicar permisos de seguridad
    @FXML private Label lblAdministracion;
    @FXML private Button btnUsuarios;
    @FXML private Button btnGuardar;

    private final InventarioDAO inventarioDAO = new InventarioDAO();
    private ObservableList<ValidacionItem> lista = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        if (Session.getNombreCompleto() != null) {
            lblUsuario.setText(Session.getNombreCompleto() + " (" + Session.getRol() + ")");
            aplicarPermisos(); // Aplica la seguridad al iniciar
        }
        
        // La tabla inicia editable, pero aplicarPermisos() puede bloquearla si es cajero
        tablaValidacion.setEditable(true); 
        cargarColumnas();
        cargarDatos();
    }

    private void aplicarPermisos() {
        String rol = Session.getRol();
        if (rol == null) return;

        if ("CAJERO".equals(rol)) {
            // 1. Ocultar sección de Administración del menú lateral
            if (lblAdministracion != null) {
                lblAdministracion.setVisible(false);
                lblAdministracion.setManaged(false); 
            }
            if (btnUsuarios != null) {
                btnUsuarios.setVisible(false);
                btnUsuarios.setManaged(false); 
            }
            
            // 2. Bloquear acciones de escritura de inventario
            if (btnGuardar != null) {
                btnGuardar.setVisible(false);
                btnGuardar.setManaged(false);
            }
            if (tablaValidacion != null) {
                tablaValidacion.setEditable(false);
            }
        }
    }

    private void cargarColumnas() {

        TableColumn<ValidacionItem, String> colProducto = new TableColumn<>("Producto");
        colProducto.setPrefWidth(220);
        colProducto.setCellValueFactory(c -> c.getValue().nombreProperty());
        colProducto.setEditable(false);

        TableColumn<ValidacionItem, Integer> colSistema = new TableColumn<>("En sistema");
        colSistema.setPrefWidth(110);
        colSistema.setCellValueFactory(c -> c.getValue().enSistemaProperty().asObject());
        colSistema.setEditable(false);

        // Columna editable: En físico
        TableColumn<ValidacionItem, Integer> colFisico = new TableColumn<>("En físico");
        colFisico.setPrefWidth(110);
        colFisico.setCellValueFactory(c -> c.getValue().enFisicoProperty().asObject());
        colFisico.setEditable(true);
        colFisico.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter() {
            @Override
            public Integer fromString(String s) {
                try { return Integer.parseInt(s.trim()); }
                catch (NumberFormatException e) { return 0; }
            }
        }));
        colFisico.setOnEditCommit(event -> {
            ValidacionItem item = event.getRowValue();
            int valor = event.getNewValue() == null ? 0 : event.getNewValue();
            if (valor < 0) valor = 0;
            item.actualizarFisico(valor);
            tablaValidacion.refresh();
            actualizarResumen();
        });

        TableColumn<ValidacionItem, Integer> colDif = new TableColumn<>("Diferencia");
        colDif.setPrefWidth(100);
        colDif.setCellValueFactory(c -> c.getValue().diferenciaProperty().asObject());
        colDif.setEditable(false);

        // Columna estado con colores
        TableColumn<ValidacionItem, String> colEstado = new TableColumn<>("Estado");
        colEstado.setPrefWidth(130);
        colEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        colEstado.setEditable(false);
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setText(null); setStyle(""); return; }
                setText(estado);
                switch (estado) {
                    case "OK" ->
                        setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                                 "-fx-font-weight: bold; -fx-alignment: CENTER;");
                    case "Sin capturar" ->
                        setStyle("-fx-background-color: #e9ecef; -fx-text-fill: #666666; " +
                                 "-fx-alignment: CENTER;");
                    default -> {
                        if (estado.startsWith("Faltan"))
                            setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                                     "-fx-font-weight: bold; -fx-alignment: CENTER;");
                        else
                            setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; " +
                                     "-fx-font-weight: bold; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        tablaValidacion.getColumns().setAll(colProducto, colSistema, colFisico, colDif, colEstado);
    }

    private void cargarDatos() {
        lista.clear();
        try {
            lista.addAll(inventarioDAO.obtenerProductosActivos());
            tablaValidacion.setItems(lista);
            actualizarResumen();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar productos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void actualizarResumen() {
        long sinCapturar = lista.stream().filter(i -> !i.isCapturado()).count();
        long ok          = lista.stream().filter(i -> i.isCapturado() && i.getEstado().equals("OK")).count();
        long faltan      = lista.stream().filter(i -> i.getEstado().startsWith("Faltan")).count();
        long sobran      = lista.stream().filter(i -> i.getEstado().startsWith("Sobran")).count();

        lblResumen.setText(
            "⬜ Sin capturar: " + sinCapturar + "   " +
            "✅ OK: " + ok + "   " +
            "🔴 Faltantes: " + faltan + "   " +
            "🟡 Excedentes: " + sobran
        );
    }

    @FXML
    private void guardarValidacion() {
        long capturados = lista.stream().filter(ValidacionItem::isCapturado).count();

        if (capturados == 0) {
            mostrarAlerta("No has capturado ninguna cantidad física.\n" +
                          "Haz doble clic en la columna 'En físico' para ingresar los valores.",
                          Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("¿Guardar validación?");
        confirm.setContentText("Se actualizarán " + capturados + " producto(s) en el sistema.\n" +
                               "Los productos sin capturar NO se modificarán.");

        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    int actualizados = inventarioDAO.guardarValidacion(lista);
                    mostrarAlerta("Se actualizaron " + actualizados + " producto(s) correctamente.\n" +
                                  "Los demás conservaron su cantidad original.",
                                  Alert.AlertType.INFORMATION);
                    cargarDatos();
                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarAlerta("Error al guardar: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void recargarDatos() { cargarDatos(); }

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
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void logout() {
        Session.cerrar();
        cambiarVista("/fxml/login.fxml");
    }

    private void mostrarAlerta(String msg, Alert.AlertType tipo) {
        Alert a = new Alert(tipo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}