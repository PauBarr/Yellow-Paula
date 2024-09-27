package com.yellow;

import java.util.Date;
import java.util.List;

public class Receta {

    private int id;
    private String nombre;
    private String descripcion;
    private double costoTotal;
    private Date fechaCreacion;
    private int tiempoPreparacion;
    private List<Ingrediente> ingredientes;
    private List<Categoria> categorias;
    private String nombreReceta;
    private List<RecetaIngrediente> recetaIngredientes; // Relación con la tabla intermedia

    // Constructor con todos los atributos
    public Receta(int id, String nombre, String descripcion, double costoTotal, Date fechaCreacion, 
                  int tiempoPreparacion, List<Ingrediente> ingredientes, List<Categoria> categorias) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costoTotal = costoTotal;
        this.fechaCreacion = fechaCreacion;
        this.tiempoPreparacion = tiempoPreparacion;
        this.ingredientes = ingredientes;
        this.categorias = categorias;
    }

    public Receta(double parseDouble, double parseDouble2, double parseDouble3, double parseDouble4) {
		// TODO Auto-generated constructor stub
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
    public String getNombreReceta() {
        return nombreReceta;
    }

    public void setNombreReceta(String nombreReceta) {
        this.nombreReceta = nombreReceta;
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
    public List<RecetaIngrediente> getRecetaIngredientes() {
        return recetaIngredientes;
    }

    public void setRecetaIngredientes(List<RecetaIngrediente> recetaIngredientes) {
        this.recetaIngredientes = recetaIngredientes;
    }
}
