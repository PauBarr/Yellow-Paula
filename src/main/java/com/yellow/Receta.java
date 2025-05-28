package com.yellow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Receta {

    private int id; //
    private String nombre; //
    private String descripcion; //
    private double costoTotal; //
    private Date fechaCreacion; //
    private int tiempoPreparacion; //
    // Cambia a una lista de RecetaIngrediente para la relación Many-to-Many con atributos
    private List<RecetaIngrediente> recetaIngredientes = new ArrayList<>(); // CAMBIO AQUÍ
    private List<Categoria> categorias = new ArrayList<>(); //

    public Receta() {} //

    public Receta(String nombre, String descripcion) { //
        this.nombre = nombre; //
        this.descripcion = descripcion; //
        this.costoTotal = 0.0; //
        this.fechaCreacion = new Date(); //
        this.tiempoPreparacion = 0; //
    }

    // Eliminar o ajustar estos métodos si ya no tienen sentido directo aquí,
    // ya que la relación es ahora a través de RecetaIngrediente
    public String getProducto() { //
        // Podrías devolver el producto del primer ingrediente de recetaIngredientes si es necesario un "principal"
        return recetaIngredientes.isEmpty() ? null : recetaIngredientes.get(0).getIngrediente().getNombre(); //
    }

    public String getTipoPesoLt() { //
        return recetaIngredientes.isEmpty() ? null : recetaIngredientes.get(0).getIngrediente().getTipoPesoLt(); //
    }

    public double getPesoLtR() { //
        return recetaIngredientes.isEmpty() ? 0.0 : recetaIngredientes.get(0).getIngrediente().getPesoLtR(); //
    }

    public double getCostoUnitario() { //
        return recetaIngredientes.isEmpty() ? 0.0 : recetaIngredientes.get(0).getIngrediente().getCostoUnitario(); //
    }

    public double getCantidadUtilizada() { //
        return recetaIngredientes.isEmpty() ? 0.0 : recetaIngredientes.get(0).getCantidadUtilizada(); //
    }

    public double getCostoReal() { //
        return recetaIngredientes.isEmpty() ? 0.0 : recetaIngredientes.get(0).getCostoReal(); //
    }

    // Método para recalcular el costo total de la receta
    public void calcularCostoTotal() { //
        this.costoTotal = recetaIngredientes.stream() // CAMBIO AQUÍ
            .mapToDouble(RecetaIngrediente::getCostoReal) // CAMBIO AQUÍ
            .sum(); //
    }


    public int getId() { //
        return id; //
    }

    public void setId(int id) { //
        this.id = id; //
    }

    public String getNombre() { //
        return nombre; //
    }

    public void setNombre(String nombre) { //
        this.nombre = nombre; //
    }

    public String getDescripcion() { //
        return descripcion; //
    }

    public void setDescripcion(String descripcion) { //
        this.descripcion = descripcion; //
    }

    public double getCostoTotal() { //
        return costoTotal; //
    }

    public void setCostoTotal(double costoTotal) { //
        this.costoTotal = costoTotal; //
    }

    public Date getFechaCreacion() { //
        return fechaCreacion; //
    }

    public void setFechaCreacion(Date fechaCreacion) { //
        this.fechaCreacion = fechaCreacion; //
    }

    public int getTiempoPreparacion() { //
        return tiempoPreparacion; //
    }

    public void setTiempoPreparacion(int tiempoPreparacion) { //
        this.tiempoPreparacion = tiempoPreparacion; //
    }

    public List<RecetaIngrediente> getRecetaIngredientes() { // CAMBIO AQUÍ
        return recetaIngredientes; //
    }

    public void setRecetaIngredientes(List<RecetaIngrediente> recetaIngredientes) { // CAMBIO AQUÍ
        this.recetaIngredientes = recetaIngredientes; //
    }

    public List<Categoria> getCategorias() { //
        return categorias; //
    }

    public void setCategorias(List<Categoria> categorias) { //
        this.categorias = categorias; //
    }

    // Nuevo método para recalcular el costo total de la receta
    public void recalcularCostoTotal() {
        double nuevoCostoTotal = 0.0;
        // Itera sobre cada ingrediente de la receta
        for (RecetaIngrediente ri : this.recetaIngredientes) {
            // Suma el costo real de cada ingrediente (cantidad utilizada * costo unitario actual del ingrediente)
            nuevoCostoTotal += ri.getCantidadUtilizada() * ri.getIngrediente().getCostoUnitario();
        }
        this.costoTotal = nuevoCostoTotal; // Actualiza el costo total de la receta
    }

    public void mostrarDetallesIngredientes() { //
        for (RecetaIngrediente ri : recetaIngredientes) { // CAMBIO AQUÍ
            System.out.println("Producto: " + ri.getIngrediente().getNombre()); //
            System.out.println("Tipo de Peso: " + ri.getIngrediente().getTipoPesoLt()); //
            System.out.println("Peso/Litro Real: " + ri.getIngrediente().getPesoLtR()); //
            System.out.println("Costo Unitario: " + ri.getIngrediente().getCostoUnitario()); //
            System.out.println("Cantidad Utilizada: " + ri.getCantidadUtilizada()); //
            System.out.println("Costo Real: " + ri.getCostoReal()); //
            System.out.println("-----------------------------"); //
        }
    }
}