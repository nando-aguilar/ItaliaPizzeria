package com.italia.pizza.models;

import javafx.beans.property.*;

public class Producto {

    private IntegerProperty idProducto;
    private StringProperty  codigo;
    private StringProperty  nombre;
    private StringProperty  descripcion;
    private DoubleProperty  precio;
    private StringProperty  restricciones;
    private IntegerProperty cantidad;
    private StringProperty  estatus;

    public Producto(int idProducto, String codigo, String nombre, String descripcion,
                    double precio, String restricciones, int cantidad, String estatus) {
        this.idProducto   = new SimpleIntegerProperty(idProducto);
        this.codigo       = new SimpleStringProperty(codigo);
        this.nombre       = new SimpleStringProperty(nombre);
        this.descripcion  = new SimpleStringProperty(descripcion);
        this.precio       = new SimpleDoubleProperty(precio);
        this.restricciones = new SimpleStringProperty(restricciones == null ? "" : restricciones);
        this.cantidad     = new SimpleIntegerProperty(cantidad);
        this.estatus      = new SimpleStringProperty(estatus);
    }

    public int     getIdProducto()               { return idProducto.get(); }
    public IntegerProperty idProductoProperty()  { return idProducto; }

    public String  getCodigo()                   { return codigo.get(); }
    public StringProperty codigoProperty()       { return codigo; }
    public void    setCodigo(String v)           { codigo.set(v); }

    public String  getNombre()                   { return nombre.get(); }
    public StringProperty nombreProperty()       { return nombre; }
    public void    setNombre(String v)           { nombre.set(v); }

    public String  getDescripcion()              { return descripcion.get(); }
    public StringProperty descripcionProperty()  { return descripcion; }
    public void    setDescripcion(String v)      { descripcion.set(v); }

    public double  getPrecio()                   { return precio.get(); }
    public DoubleProperty precioProperty()       { return precio; }
    public void    setPrecio(double v)           { precio.set(v); }

    public String  getRestricciones()            { return restricciones.get(); }
    public StringProperty restriccionesProperty(){ return restricciones; }
    public void    setRestricciones(String v)    { restricciones.set(v == null ? "" : v); }

    public int     getCantidad()                 { return cantidad.get(); }
    public IntegerProperty cantidadProperty()    { return cantidad; }
    public void    setCantidad(int v)            { cantidad.set(v); }

    public String  getEstatus()                  { return estatus.get(); }
    public StringProperty estatusProperty()      { return estatus; }
    public void    setEstatus(String v)          { estatus.set(v); }
}