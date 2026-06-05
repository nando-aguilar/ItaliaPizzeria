package com.italia.pizza.models;

import javafx.beans.property.*;

public class UsuarioCompleto {

    private final IntegerProperty idUsuario;
    private final StringProperty nombre;
    private final StringProperty apellidos;
    private final StringProperty telefono;
    private final StringProperty email;
    private final StringProperty calleNumero;
    private final StringProperty codigoPostal;
    private final StringProperty ciudad;
    private final StringProperty tipo;
    private final StringProperty nombreUsuario;
    private final StringProperty contrasena;
    private final StringProperty estatus;

    public UsuarioCompleto(int idUsuario, String nombre, String apellidos,
                   String telefono, String email, String calleNumero,
                   String codigoPostal, String ciudad, String tipo,
                   String nombreUsuario, String contrasena, String estatus) {
        this.idUsuario    = new SimpleIntegerProperty(idUsuario);
        this.nombre       = new SimpleStringProperty(nombre);
        this.apellidos    = new SimpleStringProperty(apellidos);
        this.telefono     = new SimpleStringProperty(telefono);
        this.email        = new SimpleStringProperty(email);
        this.calleNumero  = new SimpleStringProperty(calleNumero);
        this.codigoPostal = new SimpleStringProperty(codigoPostal);
        this.ciudad       = new SimpleStringProperty(ciudad);
        this.tipo         = new SimpleStringProperty(tipo);
        this.nombreUsuario= new SimpleStringProperty(nombreUsuario);
        this.contrasena   = new SimpleStringProperty(contrasena);
        this.estatus      = new SimpleStringProperty(estatus);
    }

    public int getIdUsuario()                    { return idUsuario.get(); }
    public IntegerProperty idUsuarioProperty()   { return idUsuario; }

    public String getNombre()                    { return nombre.get() + " " + apellidos.get(); }
    public StringProperty nombreProperty() {
        return new SimpleStringProperty(getNombre());
    }

    public String getNombreSolo()                { return nombre.get(); }
    public String getApellidos()                 { return apellidos.get(); }

    public String getTelefono()                  { return telefono.get(); }
    public StringProperty telefonoProperty()     { return telefono; }

    public String getEmail()                     { return email.get(); }
    public StringProperty emailProperty()        { return email; }

    public String getCalleNumero()               { return calleNumero.get(); }

    public String getCodigoPostal()              { return codigoPostal.get(); }

    public String getCiudad()                    { return ciudad.get(); }

    public String getTipo()                      { return tipo.get(); }
    public StringProperty tipoProperty()         { return tipo; }

    public String getNombreUsuario()             { return nombreUsuario.get(); }

    public String getContrasena()                { return contrasena.get(); }

    public String getEstatus()                   { return estatus.get(); }
    public StringProperty estatusProperty()      { return estatus; }
}