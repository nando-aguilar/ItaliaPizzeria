package com.italia.pizza.models;
 
import javafx.beans.property.*;
 
public class ValidacionItem {
 
    private final IntegerProperty idProducto;
    private final StringProperty  nombre;
    private final IntegerProperty enSistema;
    private final IntegerProperty enFisico;
    private final IntegerProperty diferencia;
    private final StringProperty  estado;
    private boolean capturado; // true solo si el usuario ingresó un valor
 
    public ValidacionItem(int idProducto, String nombre, int enSistema) {
        this.idProducto = new SimpleIntegerProperty(idProducto);
        this.nombre     = new SimpleStringProperty(nombre);
        this.enSistema  = new SimpleIntegerProperty(enSistema);
        this.enFisico   = new SimpleIntegerProperty(enSistema); // inicia igual al sistema
        this.diferencia = new SimpleIntegerProperty(0);          // sin diferencia inicial
        this.estado     = new SimpleStringProperty("Sin capturar");
        this.capturado  = false;
    }
 
    public void actualizarFisico(int fisico) {
        capturado = true;
        enFisico.set(fisico);
        int diff = fisico - enSistema.get();
        diferencia.set(diff);
        if (diff == 0)     estado.set("OK");
        else if (diff < 0) estado.set("Faltan " + Math.abs(diff));
        else               estado.set("Sobran " + diff);
    }
 
    public boolean isCapturado() { return capturado; }
 
    public int    getIdProducto()               { return idProducto.get(); }
    public IntegerProperty idProductoProperty() { return idProducto; }
 
    public String getNombre()                   { return nombre.get(); }
    public StringProperty nombreProperty()      { return nombre; }
 
    public int    getEnSistema()                { return enSistema.get(); }
    public IntegerProperty enSistemaProperty()  { return enSistema; }
 
    public int    getEnFisico()                 { return enFisico.get(); }
    public IntegerProperty enFisicoProperty()   { return enFisico; }
 
    public int    getDiferencia()               { return diferencia.get(); }
    public IntegerProperty diferenciaProperty() { return diferencia; }
 
    public String getEstado()                   { return estado.get(); }
    public StringProperty estadoProperty()      { return estado; }
}