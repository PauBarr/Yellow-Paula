package com.yellow;

public class Papeleria {
    private int id;
    private String nombreProducto;
    private int stock;
    private String descripcionMedida;
    private boolean enStock; // true si hay stock, false si no

    public Papeleria() {
    }

    public Papeleria(String nombreProducto, int stock, String descripcionMedida, boolean enStock) {
        this.nombreProducto = nombreProducto;
        this.stock = stock;
        this.descripcionMedida = descripcionMedida;
        this.enStock = enStock;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getDescripcionMedida() {
        return descripcionMedida;
    }

    public void setDescripcionMedida(String descripcionMedida) {
        this.descripcionMedida = descripcionMedida;
    }

    public boolean isEnStock() {
        return enStock;
    }

    public void setEnStock(boolean enStock) {
        this.enStock = enStock;
    }

    @Override
    public String toString() {
        return nombreProducto;
    }
}