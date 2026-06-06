package com.italia.pizza.controllers;

import com.italia.pizza.dao.UsuarioDAO;
import com.italia.pizza.models.Usuario;
import com.italia.pizza.models.UsuarioCompleto;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class UsuariosFormController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtCP;
    @FXML private TextField txtCiudad;
    @FXML private ComboBox<String> comboTipo;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblTitulo;

    private Usuario usuarioEditando = null;
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    private void initialize() {
        comboTipo.getItems().addAll("Empleado", "Cliente");

        // Mostrar/ocultar sección empleado según tipo seleccionado
        comboTipo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean esEmpleado = "Empleado".equals(newVal);
            txtUsuario.setDisable(!esEmpleado);
            txtPassword.setDisable(!esEmpleado);
            if (!esEmpleado) {
                txtUsuario.clear();
                txtPassword.clear();
            }
        });
    }

    // ---------------------------------------------------------
    // CARGAR DATOS PARA EDICIÓN
    // Recibe el Usuario "ligero" de la tabla, luego busca el
    // UsuarioCompleto en BD para tener todos los campos.
    // ---------------------------------------------------------
    public void cargarUsuario(Usuario u) {
        this.usuarioEditando = u;

        try {
            usuarioDAO.buscarPorId(u.getIdUsuario()).ifPresent(completo -> {
                txtNombre.setText(completo.getNombreSolo());
                txtApellidos.setText(completo.getApellidos());
                txtTelefono.setText(completo.getTelefono());
                txtEmail.setText(completo.getEmail());
                txtDireccion.setText(completo.getCalleNumero());
                txtCP.setText(completo.getCodigoPostal());
                txtCiudad.setText(completo.getCiudad());
                comboTipo.setValue(completo.getTipo());
                txtUsuario.setText(completo.getNombreUsuario());
                txtPassword.setText(completo.getContrasena());
            });
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar datos del usuario", Alert.AlertType.ERROR);
        }
    }

    // ---------------------------------------------------------
    // GUARDAR
    // ---------------------------------------------------------
    @FXML
private void guardarUsuario() {
    if (!validarCampos()) return;

    try {
        if (usuarioEditando == null) {
            // --- MODO REGISTRO ---
            UsuarioCompleto completo = construirUsuarioCompletoNuevo();
            boolean ok = usuarioDAO.insertarUsuario(completo);

            if (ok) {
                mostrarAlerta("Usuario registrado correctamente", Alert.AlertType.INFORMATION);
                cerrarVentana();
            } else {
                mostrarAlerta("No se pudo registrar el usuario", Alert.AlertType.ERROR);
            }

        } else {
            // --- MODO EDICIÓN (código que ya tenías) ---
            UsuarioCompleto completo = construirUsuarioCompleto();
            boolean ok = usuarioDAO.actualizarUsuario(completo);

            if (ok) {
                usuarioEditando.setNombre(txtNombre.getText() + " " + txtApellidos.getText());
                usuarioEditando.setTelefono(txtTelefono.getText());
                usuarioEditando.setEmail(txtEmail.getText());
                usuarioEditando.setTipo(comboTipo.getValue());

                mostrarAlerta("Usuario actualizado correctamente", Alert.AlertType.INFORMATION);
                cerrarVentana();
            } else {
                mostrarAlerta("No se pudo actualizar el usuario", Alert.AlertType.ERROR);
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
        mostrarAlerta("Error: " + e.getMessage(), Alert.AlertType.ERROR);
    }
}
private UsuarioCompleto construirUsuarioCompletoNuevo() {
    boolean esEmpleado = "Empleado".equals(comboTipo.getValue());
    return new UsuarioCompleto(
            0, // id_usuario = 0, la BD lo genera con AUTO_INCREMENT
            txtNombre.getText().trim(),
            txtApellidos.getText().trim(),
            txtTelefono.getText().trim(),
            txtEmail.getText().trim(),
            txtDireccion.getText().trim(),
            txtCP.getText().trim(),
            txtCiudad.getText().trim(),
            comboTipo.getValue(),
            esEmpleado ? txtUsuario.getText().trim() : null,
            esEmpleado ? txtPassword.getText() : null,
            "Activo" // estatus por defecto
    );
}

    private UsuarioCompleto construirUsuarioCompleto() {
        boolean esEmpleado = "Empleado".equals(comboTipo.getValue());
        return new UsuarioCompleto(
                usuarioEditando.getIdUsuario(),
                txtNombre.getText().trim(),
                txtApellidos.getText().trim(),
                txtTelefono.getText().trim(),
                txtEmail.getText().trim(),
                txtDireccion.getText().trim(),
                txtCP.getText().trim(),
                txtCiudad.getText().trim(),
                comboTipo.getValue(),
                esEmpleado ? txtUsuario.getText().trim() : null,
                esEmpleado ? txtPassword.getText() : null,
                usuarioEditando.getEstatus()
        );
    }

    private boolean validarCampos() {
        if (txtNombre.getText().isBlank() ||
                txtApellidos.getText().isBlank() ||
                txtTelefono.getText().isBlank() ||
                txtEmail.getText().isBlank() ||
                txtDireccion.getText().isBlank() ||
                txtCP.getText().isBlank() ||
                txtCiudad.getText().isBlank() ||
                comboTipo.getValue() == null) {

            mostrarAlerta("Todos los campos obligatorios deben completarse", Alert.AlertType.WARNING);
            return false;
        }

        if ("Empleado".equals(comboTipo.getValue()) &&
                (txtUsuario.getText().isBlank() || txtPassword.getText().isBlank())) {

            mostrarAlerta("Los empleados deben tener usuario y contraseña", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }
    public void modoRegistro() {
        this.usuarioEditando = null;
        lblTitulo.setText("Registrar Usuario");
    }

    @FXML
    private void cancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
