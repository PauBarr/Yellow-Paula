package com.yellow;

public class RecetaIngrediente {
	private int id;
	private Receta receta;
	private Ingrediente ingrediente;
	private double cantidadUtilizada;

	// Constructor
	public RecetaIngrediente(Receta receta, Ingrediente ingrediente, double cantidadUtilizada) {
		this.receta = receta;
		this.ingrediente = ingrediente;
		this.cantidadUtilizada = cantidadUtilizada;
	}

	public RecetaIngrediente() {
	}

	// Getters y Setters
	public Receta getReceta() {
		return receta;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setReceta(Receta receta) {
		this.receta = receta;
	}

	public Ingrediente getIngrediente() {
		return ingrediente;
	}

	public void setIngrediente(Ingrediente ingrediente) {
		this.ingrediente = ingrediente;
	}

	public double getCantidadUtilizada() {
		return cantidadUtilizada;
	}

	public void setCantidadUtilizada(double cantidadUtilizada) {
		this.cantidadUtilizada = cantidadUtilizada;
	}

    // Nuevo método para obtener el costo real de este RecetaIngrediente
    public double getCostoReal() {
        if (ingrediente != null) {
            return cantidadUtilizada * ingrediente.getCostoUnitario();
        }
        return 0.0;
    }

	public void setCostoTotal(double costoTotal) {
		// TODO Auto-generated method stub

	}

	public void setNombre(String nombreIngrediente) {
		// TODO Auto-generated method stub

	}

	public void setNombre1(String nombreIngrediente) {
		// TODO Auto-generated method stub

	}

}