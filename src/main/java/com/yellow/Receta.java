package com.yellow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Receta {
    private int id;
    private String nombre;
    private String descripcion;
    private Double costoTotal = 0.0;
    private Double gastosExtra = 0.0; 
    private Date fechaCreacion;
    private int tiempoPreparacion;
    private List<RecetaIngrediente> recetaIngredientes = new ArrayList<>();
    private List<Categoria> categorias = new ArrayList<>();

    public Receta() {}

    // Constructor que usa PantallaCostos
    public Receta(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costoTotal = 0.0;
        this.gastosExtra = 0.0;
        this.fechaCreacion = new Date();
        this.tiempoPreparacion = 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Double getCostoTotal() { return (costoTotal == null) ? 0.0 : costoTotal; }
    public void setCostoTotal(Double costoTotal) { this.costoTotal = costoTotal; }
    public Double getGastosExtra() { return (gastosExtra == null) ? 0.0 : gastosExtra; }
    public void setGastosExtra(Double gastosExtra) { this.gastosExtra = gastosExtra; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public int getTiempoPreparacion() { return tiempoPreparacion; }
    public void setTiempoPreparacion(int tiempoPreparacion) { this.tiempoPreparacion = tiempoPreparacion; }
    public List<RecetaIngrediente> getRecetaIngredientes() { return recetaIngredientes; }
    public void setRecetaIngredientes(List<RecetaIngrediente> recetaIngredientes) { this.recetaIngredientes = recetaIngredientes; }
    public List<Categoria> getCategorias() { return categorias; }
    public void setCategorias(List<Categoria> categorias) { this.categorias = categorias; }

    public void recalcularCostoTotal() {
        double nuevoCostoTotal = 0.0;
        for (RecetaIngrediente ri : this.recetaIngredientes) {
            nuevoCostoTotal += ri.getCostoReal();
        }
        this.costoTotal = nuevoCostoTotal + getGastosExtra();
    }
}