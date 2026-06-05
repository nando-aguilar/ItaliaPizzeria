package com.italia.pizza.models;

import javafx.beans.property.*;

public class DetallePedido {

    private IntegerProperty idDetalle;
    private IntegerProperty idPedido;
    private IntegerProperty idProducto;
    private StringProperty  nombreProducto;
    private IntegerProperty cantidad;
    private DoubleProperty  precioUnitario;

    public DetallePedido(int idDetalle, int idPedido, int idProducto,
                         String nombreProducto, int cantidad, double precioUnitario) {
        this.idDetalle      = new SimpleIntegerProperty(idDetalle);
        this.idPedido       = new SimpleIntegerProperty(idPedido);
        this.idProducto     = new SimpleIntegerProperty(idProducto);
        this.nombreProducto = new SimpleStringProperty(nombreProducto);
        this.cantidad       = new SimpleIntegerProperty(cantidad);
        this.precioUnitario = new SimpleDoubleProperty(precioUnitario);
    }

    public int     getIdDetalle()                   { return idDetalle.get(); }
    public int     getIdPedido()                    { return idPedido.get(); }
    public int     getIdProducto()                  { return idProducto.get(); }
    public IntegerProperty idProductoProperty()     { return idProducto; }

    public String  getNombreProducto()              { return nombreProducto.get(); }
    public StringProperty nombreProductoProperty()  { return nombreProducto; }
    public void    setNombreProducto(String v)      { nombreProducto.set(v); }

    public int     getCantidad()                    { return cantidad.get(); }
    public IntegerProperty cantidadProperty()       { return cantidad; }
    public void    setCantidad(int v)               { cantidad.set(v); }

    public double  getPrecioUnitario()              { return precioUnitario.get(); }
    public DoubleProperty precioUnitarioProperty()  { return precioUnitario; }
    public void    setPrecioUnitario(double v)      { precioUnitario.set(v); }

    public double  getSubtotal()                    { return cantidad.get() * precioUnitario.get(); }
}