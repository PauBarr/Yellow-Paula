package com.yellow;

public class Ingrediente {

    private int id;
    private String nombre;       // Nombre del producto o ingrediente
    private String tipoPesoLt;     // Tipo de unidad de medida del producto (ej. "gramos", "unidades", "litros")
    private double cantidadDeCompra; // Cantidad total del paquete/unidad comprada (ej. 1000.0 para 1kg de premezcla, o 1.0 para 1 banana)
    private double costoDeCompra;  // Costo total del paquete/unidad comprada (ej. 5600.0 para el paquete de premezcla, o 200.0 para la banana)

    // Constructor vacío requerido por Hibernate
    public Ingrediente() {}

    // Constructor completo actualizado
    public Ingrediente(String nombre, String tipoPesoLt, double cantidadDeCompra, double costoDeCompra) {
        this.nombre = nombre;
        this.tipoPesoLt = tipoPesoLt;
        this.cantidadDeCompra = cantidadDeCompra;
        this.costoDeCompra = costoDeCompra;
    }

    // Getters y Setters actualizados

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoPesoLt() { // Ahora representa la unidad del tipo de compra (ej. "gramos", "unidades")
        return tipoPesoLt;
    }

    public void setTipoPesoLt(String tipoPesoLt) {
        this.tipoPesoLt = tipoPesoLt;
    }

    public double getCantidadDeCompra() {
        return cantidadDeCompra;
    }

    public void setCantidadDeCompra(double cantidadDeCompra) {
        this.cantidadDeCompra = cantidadDeCompra;
    }

    public double getCostoDeCompra() {
        return costoDeCompra;
    }

    public void setCostoDeCompra(double costoDeCompra) {
        this.costoDeCompra = costoDeCompra;
    }

    // Este método calculará el costo unitario por la unidad más pequeña
    // (ej. costo por gramo, costo por ml, costo por unidad)
    public double getCostoUnitarioCalculado() {
        if (cantidadDeCompra > 0) {
            return costoDeCompra / cantidadDeCompra;
        }
        return 0.0;
    }

    // Método toString para mostrar el nombre del producto en JComboBox
    @Override
    public String toString() {
        return nombre;
    }
}