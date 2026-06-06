package com.italia.pizza.models;

import javafx.beans.property.SimpleStringProperty;

public class MenuTablas {

    // Sub-clase para Productos
    public static class ProductoVista {
        public final SimpleStringProperty id, nombre, codigo, precio, cantidad, estatus;
        
        public ProductoVista(String id, String nombre, String codigo, String precio, String cantidad, String estatus) {
            this.id = new SimpleStringProperty(id);
            this.nombre = new SimpleStringProperty(nombre);
            this.codigo = new SimpleStringProperty(codigo);
            this.precio = new SimpleStringProperty(precio);
            this.cantidad = new SimpleStringProperty(cantidad);
            this.estatus = new SimpleStringProperty(estatus);
        }
    }

    // Sub-clase para Pedidos
    public static class PedidoVista {
        public final SimpleStringProperty id, fecha, total, estatus, cliente, empleado;
        
        public PedidoVista(String id, String fecha, String total, String estatus, String cliente, String empleado) {
            this.id = new SimpleStringProperty(id);
            this.fecha = new SimpleStringProperty(fecha);
            this.total = new SimpleStringProperty(total);
            this.estatus = new SimpleStringProperty(estatus);
            this.cliente = new SimpleStringProperty(cliente);
            this.empleado = new SimpleStringProperty(empleado);
        }
    }

    // Sub-clase para Detalles
    public static class DetalleVista {
        public final SimpleStringProperty id, idPedido, producto, cantidad, precio, subtotal;
        
        public DetalleVista(String id, String idPedido, String producto, String cantidad, String precio, String subtotal) {
            this.id = new SimpleStringProperty(id);
            this.idPedido = new SimpleStringProperty(idPedido);
            this.producto = new SimpleStringProperty(producto);
            this.cantidad = new SimpleStringProperty(cantidad);
            this.precio = new SimpleStringProperty(precio);
            this.subtotal = new SimpleStringProperty(subtotal);
        }
    }
    
    public static class UsuarioReciente {
    public final SimpleStringProperty id, nombre, tipo, estatus;

    public UsuarioReciente(String id, String nombre, String tipo, String estatus) {
        this.id = new SimpleStringProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.tipo = new SimpleStringProperty(tipo);
        this.estatus = new SimpleStringProperty(estatus);
    }
}
}