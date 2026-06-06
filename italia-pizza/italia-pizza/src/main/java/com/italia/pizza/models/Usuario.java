package com.italia.pizza.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Usuario {

    private final IntegerProperty idUsuario;
    private final StringProperty nombre;
    private final StringProperty telefono;
    private final StringProperty email;
    private final StringProperty tipo;
    private final StringProperty estatus;

    public Usuario(int idUsuario, String nombre, String telefono, String email, String tipo, String estatus) {
        this.idUsuario = new SimpleIntegerProperty(idUsuario);
        this.nombre = new SimpleStringProperty(nombre);
        this.telefono = new SimpleStringProperty(telefono);
        this.email = new SimpleStringProperty(email);
        this.tipo = new SimpleStringProperty(tipo);
        this.estatus = new SimpleStringProperty(estatus);
    }

    public int getIdUsuario() { return idUsuario.get(); }
    public String getNombre() { return nombre.get(); }
    public String getTelefono() { return telefono.get(); }
    public String getEmail() { return email.get(); }
    public String getTipo() { return tipo.get(); }
    public String getEstatus() { return estatus.get(); }

    public void setEstatus(String estatus) { this.estatus.set(estatus); }
    public void setNombre(String nombre)       { this.nombre.set(nombre); }
    public void setTelefono(String telefono)   { this.telefono.set(telefono); }
    public void setEmail(String email)         { this.email.set(email); }
    public void setTipo(String tipo)           { this.tipo.set(tipo); }

    public IntegerProperty idUsuarioProperty() {
        return idUsuario;
    }

    public StringProperty nombreProperty() { return nombre; }
    public StringProperty telefonoProperty() { return telefono; }
    public StringProperty emailProperty() { return email; }
    public StringProperty tipoProperty() { return tipo; }
    public StringProperty estatusProperty() { return estatus; }
}
