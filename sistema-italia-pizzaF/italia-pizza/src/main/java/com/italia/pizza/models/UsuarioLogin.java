package com.italia.pizza.models;

public class UsuarioLogin {

    private final String nombre;
    private final String apellidos;
    private final String rol;

    public UsuarioLogin(String nombre, String apellidos, String rol) {
        this.nombre    = nombre;
        this.apellidos = apellidos;
        this.rol       = rol;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    public String getRol() {
        return rol;
    }

}
