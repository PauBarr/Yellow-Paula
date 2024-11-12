package com.yellow;


public class Ingrediente {

    private int id;

    private String nombre;       // Nombre del producto o ingrediente
    private String tipoPesoLt;     // Tipo de peso o medida (Kg, Lt, etc.)
    private double pesoLtR;        // Peso o cantidad en la unidad correspondiente
    private double costoUnitario;  // Costo unitario del ingrediente
    private double cantidadUtilizada;
    private double costoReal;

    // Constructor vacío requerido por Hibernate
    public Ingrediente() {}

    // Constructor completo
    public Ingrediente(String nombre, String tipoPesoLt, double pesoLtR, double costoUnitario, double cantidadUtilizada) {
        this.nombre = nombre;
        this.tipoPesoLt = tipoPesoLt;
        this.pesoLtR = pesoLtR;
        this.costoUnitario = costoUnitario;
        this.cantidadUtilizada = cantidadUtilizada;
        this.costoReal = costoUnitario * cantidadUtilizada;
    }

    // Getters y Setters

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

    public String getTipoPesoLt() {
        return tipoPesoLt;
    }

    public void setTipoPesoLt(String tipoPesoLt) {
        this.tipoPesoLt = tipoPesoLt;
    }

    public double getPesoLtR() {
        return pesoLtR;
    }

    public void setPesoLtR(double pesoLtR) {
        this.pesoLtR = pesoLtR;
    }

    public double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(double costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public double getCantidadUtilizada() {
        return cantidadUtilizada;
    }

    public void setCantidadUtilizada(double cantidadUtilizada) {
        this.cantidadUtilizada = cantidadUtilizada;
        actualizarCostoReal();  // Actualiza el costo real automáticamente cuando se cambia la cantidad
    }

    public double getCostoReal() {
        return costoReal;
    }

    public void setCostoReal(double costoReal) {
        this.costoReal = costoReal;
    }

    // Método para actualizar el costo real en función de la cantidad utilizada y el costo unitario
    private void actualizarCostoReal() {
        this.costoReal = this.cantidadUtilizada * this.costoUnitario;
    }

    // Método toString para mostrar el nombre del producto en JComboBox
    @Override
    public String toString() {
        return nombre;
    }
}
