package com.italia.pizza.models;

import javafx.beans.property.*;

public class Pedido {

    private IntegerProperty idPedido;
    private StringProperty  fecha;
    private DoubleProperty  total;
    private StringProperty  estatus;
    private IntegerProperty idCliente;
    private StringProperty  nombreCliente;
    private IntegerProperty idEmpleado;
    private StringProperty  nombreEmpleado;

    public Pedido(int idPedido, String fecha, double total, String estatus,
                  int idCliente, String nombreCliente,
                  int idEmpleado, String nombreEmpleado) {
        this.idPedido       = new SimpleIntegerProperty(idPedido);
        this.fecha          = new SimpleStringProperty(fecha);
        this.total          = new SimpleDoubleProperty(total);
        this.estatus        = new SimpleStringProperty(estatus);
        this.idCliente      = new SimpleIntegerProperty(idCliente);
        this.nombreCliente  = new SimpleStringProperty(nombreCliente);
        this.idEmpleado     = new SimpleIntegerProperty(idEmpleado);
        this.nombreEmpleado = new SimpleStringProperty(nombreEmpleado);
    }

    public int     getIdPedido()                  { return idPedido.get(); }
    public IntegerProperty idPedidoProperty()     { return idPedido; }

    public String  getFecha()                     { return fecha.get(); }
    public StringProperty fechaProperty()         { return fecha; }
    public void    setFecha(String v)             { fecha.set(v); }

    public double  getTotal()                     { return total.get(); }
    public DoubleProperty totalProperty()         { return total; }
    public void    setTotal(double v)             { total.set(v); }

    public String  getEstatus()                   { return estatus.get(); }
    public StringProperty estatusProperty()       { return estatus; }
    public void    setEstatus(String v)           { estatus.set(v); }

    public int     getIdCliente()                 { return idCliente.get(); }
    public IntegerProperty idClienteProperty()    { return idCliente; }
    public void    setIdCliente(int v)            { idCliente.set(v); }

    public String  getNombreCliente()             { return nombreCliente.get(); }
    public StringProperty nombreClienteProperty() { return nombreCliente; }
    public void    setNombreCliente(String v)     { nombreCliente.set(v); }

    public int     getIdEmpleado()                { return idEmpleado.get(); }
    public IntegerProperty idEmpleadoProperty()   { return idEmpleado; }
    public void    setIdEmpleado(int v)           { idEmpleado.set(v); }

    public String  getNombreEmpleado()             { return nombreEmpleado.get(); }
    public StringProperty nombreEmpleadoProperty() { return nombreEmpleado; }
    public void    setNombreEmpleado(String v)     { nombreEmpleado.set(v); }
}