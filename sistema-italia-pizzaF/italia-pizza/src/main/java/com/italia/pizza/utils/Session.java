package com.italia.pizza.utils;
 
public class Session {
 
    private static String nombreCompleto;
    private static String rol;
 
    public static void setUsuario(String nombre, String rolUsuario) {
        nombreCompleto = nombre;
        rol = rolUsuario;
    }
 
    public static String getNombreCompleto() {
        return nombreCompleto;
    }
 
    public static String getRol() {
        return rol;
    }
 
    public static void cerrar() {
        nombreCompleto = null;
        rol = null;
    }
}