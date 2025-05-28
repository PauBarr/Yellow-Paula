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
    private List<RecetaIngrediente> recetaIngredientes = new ArrayList<>();
    private List<Categoria> categorias = new ArrayList<>();

    public Receta() {}

    public Receta(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costoTotal = 0.0;
        this.fechaCreacion = new Date();
        this.tiempoPreparacion = 0;
    }

    // Los siguientes métodos se eliminan o se ajustan si ya no tienen sentido directo aquí.
    // Si realmente los necesitas, se tendrían que refactorizar para iterar sobre `recetaIngredientes`.
    /*
    public String getProducto() {
        return recetaIngredientes.isEmpty() ? null : recetaIngredientes.get(0).getIngrediente().getNombre();
    }

    public String getTipoPesoLt() {
        return recetaIngredientes.isEmpty() ? null : recetaIngredientes.get(0).getIngrediente().getTipoPesoLt();
    }

    public double getPesoLtR() {
        return recetaIngredientes.isEmpty() ? 0.0 : recetaIngredientes.get(0).getIngrediente().getPesoLtR();
    }

    public double getCostoUnitario() {
        return recetaIngredientes.isEmpty() ? 0.0 : recetaIngredientes.get(0).getIngrediente().getCostoUnitarioCalculado(); // Ajustado
    }

    public double getCantidadUtilizada() {
        return recetaIngredientes.isEmpty() ? 0.0 : recetaIngredientes.get(0).getCantidadUtilizada();
    }

    public double getCostoReal() {
        return recetaIngredientes.isEmpty() ? 0.0 : recetaIngredientes.get(0).getCostoReal();
    }
    */

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

    public List<RecetaIngrediente> getRecetaIngredientes() {
        return recetaIngredientes;
    }

    public void setRecetaIngredientes(List<RecetaIngrediente> recetaIngredientes) {
        this.recetaIngredientes = recetaIngredientes;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }

    // Nuevo método para recalcular el costo total de la receta
    // Este método ya estaba bien y se beneficia de los cambios en RecetaIngrediente.getCostoReal()
    public void recalcularCostoTotal() {
        double nuevoCostoTotal = 0.0;
        for (RecetaIngrediente ri : this.recetaIngredientes) {
            nuevoCostoTotal += ri.getCostoReal(); // Usa el getCostoReal() de RecetaIngrediente
        }
        this.costoTotal = nuevoCostoTotal;
    }

    public void mostrarDetallesIngredientes() {
        for (RecetaIngrediente ri : recetaIngredientes) {
            System.out.println("Producto: " + ri.getIngrediente().getNombre());
            System.out.println("Unidad de Compra: " + ri.getIngrediente().getTipoPesoLt()); // Cambiado
            System.out.println("Cantidad de Compra: " + ri.getIngrediente().getCantidadDeCompra()); // Cambiado
            System.out.println("Costo de Compra: " + ri.getIngrediente().getCostoDeCompra()); // Cambiado
            System.out.println("Cantidad Utilizada: " + ri.getCantidadUtilizada() + " " + ri.getUnidadUtilizada()); // Añadida unidad utilizada
            System.out.println("Costo Real: " + ri.getCostoReal());
            System.out.println("-----------------------------");
        }
    }
}