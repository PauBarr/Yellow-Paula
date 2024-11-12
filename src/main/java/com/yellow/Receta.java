package com.yellow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Receta {

    private int id;
    private String nombre;
    private String descripcion;
    private double costoTotal;
    private Date fechaCreacion;
    private int tiempoPreparacion;
    private List<Ingrediente> ingredientes = new ArrayList<>();
    private List<Categoria> categorias = new ArrayList<>();

    public Receta() {}

    public Receta(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costoTotal = 0.0;
        this.fechaCreacion = new Date();
        this.tiempoPreparacion = 0;
    }

    public String getProducto() {
        return ingredientes.isEmpty() ? null : ingredientes.get(0).getNombre();
    }

    public String getTipoPesoLt() {
        return ingredientes.isEmpty() ? null : ingredientes.get(0).getTipoPesoLt();
    }

    public double getPesoLtR() {
        return ingredientes.isEmpty() ? 0.0 : ingredientes.get(0).getPesoLtR();
    }

    public double getCostoUnitario() {
        return ingredientes.isEmpty() ? 0.0 : ingredientes.get(0).getCostoUnitario();
    }

    public double getCantidadUtilizada() {
        return ingredientes.isEmpty() ? 0.0 : ingredientes.get(0).getCantidadUtilizada();
    }

    public double getCostoReal() {
        return ingredientes.isEmpty() ? 0.0 : ingredientes.get(0).getCostoReal();
    }

    public void calcularCostoTotal() {
        this.costoTotal = ingredientes.stream()
            .mapToDouble(Ingrediente::getCostoReal)
            .sum();
    }


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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(double costoTotal) {
        this.costoTotal = costoTotal;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public int getTiempoPreparacion() {
        return tiempoPreparacion;
    }

    public void setTiempoPreparacion(int tiempoPreparacion) {
        this.tiempoPreparacion = tiempoPreparacion;
    }

    public List<Ingrediente> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(List<Ingrediente> ingredientes) {
        this.ingredientes = ingredientes;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }

public void mostrarDetallesIngredientes() {
    for (Ingrediente ingrediente : ingredientes) {
        System.out.println("Producto: " + ingrediente.getNombre());
        System.out.println("Tipo de Peso: " + ingrediente.getTipoPesoLt());
        System.out.println("Peso/Litro Real: " + ingrediente.getPesoLtR());
        System.out.println("Costo Unitario: " + ingrediente.getCostoUnitario());
        System.out.println("Cantidad Utilizada: " + ingrediente.getCantidadUtilizada());
        System.out.println("Costo Real: " + ingrediente.getCostoReal());
        System.out.println("-----------------------------");

    }
}
}


