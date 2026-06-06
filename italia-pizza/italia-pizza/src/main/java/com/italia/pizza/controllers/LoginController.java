package com.italia.pizza.controllers;
 
import com.italia.pizza.dao.UsuarioDAO;
import com.italia.pizza.models.UsuarioLogin;
import com.italia.pizza.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
 
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
 
public class LoginController {
 
    private static final String TITULO_VENTANA_MENU = "Italia Pizzería - Panel Principal";
    private static final String RUTA_FXML_MENU      = "/fxml/menu.fxml";
 
    @FXML private TextField     txtUsuario;
    @FXML private PasswordField txtPassword;
 
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
 
    @FXML
    private void handleLogin() {
        String usuario  = txtUsuario.getText().trim();
        String password = txtPassword.getText().trim();
 
        if (camposVacios(usuario, password)) {
            mostrarAlerta("Debes llenar todos los campos.");
            return;
        }
 
        autenticarUsuario(usuario, password);
    }
 
    private boolean camposVacios(String usuario, String password) {
        return usuario.isEmpty() || password.isEmpty();
    }
 
    private void autenticarUsuario(String usuario, String password) {
        try {
            Optional<UsuarioLogin> resultado = usuarioDAO.buscarPorCredenciales(usuario, password);
            resultado.ifPresentOrElse(this::abrirMenu, this::mostrarErrorCredenciales);
 
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo conectar a la base de datos.");
        }
    }
 
    private void mostrarErrorCredenciales() {
        mostrarAlerta("Usuario o contraseña incorrectos.");
    }
 
    private void abrirMenu(UsuarioLogin usuarioLogin) {
        try {
            // Guardar en sesión global para que todos los módulos lo lean -/
            Session.setUsuario(usuarioLogin.getNombreCompleto(), usuarioLogin.getRol());
 
            FXMLLoader loader = cargarFxml(RUTA_FXML_MENU);
            configurarMenuController(loader, usuarioLogin);
            mostrarVentanaMenu(loader.getRoot());
            cerrarVentanaLogin();
 
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo abrir el menú principal.");
        }
    }
 
    private FXMLLoader cargarFxml(String ruta) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
        loader.load();
        return loader;
    }
 
    private void configurarMenuController(FXMLLoader loader, UsuarioLogin usuarioLogin) {
        MenuController menuController = loader.getController();
        menuController.setUsuarioLogueado(usuarioLogin.getNombreCompleto(), usuarioLogin.getRol());
    }
 
    private void mostrarVentanaMenu(Parent root) {
        Stage stage = new Stage();
        stage.setTitle(TITULO_VENTANA_MENU);
        stage.setScene(new Scene(root));
        stage.show();
    }
 
    private void cerrarVentanaLogin() {
        txtUsuario.getScene().getWindow().hide();
    }
 
    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.show();
    }
}