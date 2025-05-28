package com.yellow;

public class RecetaIngrediente {
	private int id;
	private Receta receta;
	private Ingrediente ingrediente;
	private double cantidadUtilizada;
    private String unidadUtilizada; // Nuevo campo para guardar la unidad utilizada

	// Constructor actualizado
	public RecetaIngrediente(Receta receta, Ingrediente ingrediente, double cantidadUtilizada) {
		this.receta = receta;
		this.ingrediente = ingrediente;
		this.cantidadUtilizada = cantidadUtilizada;
        // La unidadUtilizada se establecerá después, ya que se selecciona en la tabla.
        this.unidadUtilizada = ""; // Valor por defecto
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

    public String getUnidadUtilizada() { // Getter para el nuevo campo
        return unidadUtilizada;
    }

    public void setUnidadUtilizada(String unidadUtilizada) { // Setter para el nuevo campo
        this.unidadUtilizada = unidadUtilizada;
    }

    // Método para obtener el costo real de este RecetaIngrediente
    // Ahora utiliza el costo unitario calculado del Ingrediente
    public double getCostoReal() {
        if (ingrediente != null) {
            // Usa el costo unitario calculado del ingrediente
            return cantidadUtilizada * ingrediente.getCostoUnitarioCalculado();
        }
        return 0.0;
    }

    // setCostoTotal era un TODO, pero ahora getCostoReal lo calcula.
    // Si necesitas guardar el costo total final calculado por alguna razón,
    // puedes tener una propiedad adicional o renombrar esta.
    // Por ahora, la eliminaremos ya que getCostoReal() lo calcula dinámicamente.
    /*
	public void setCostoTotal(double costoTotal) {
		// TODO Auto-generated method stub
	}
    */

	// Estos métodos parecen obsoletos o no usados
	public void setNombre(String nombreIngrediente) {
		// TODO Auto-generated method stub
	}

	public void setNombre1(String nombreIngrediente) {
		// TODO Auto-generated method stub
	}

	public void setCostoTotal(double costoRealParaEsteIngrediente) {
		// TODO Auto-generated method stub
		
	}

	public void setCostoTotal1(double costoTotal) {
		// TODO Auto-generated method stub
		
	}
}