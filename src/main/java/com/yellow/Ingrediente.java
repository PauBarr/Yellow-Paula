package com.yellow;

import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "ingredientes")
public class Ingrediente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT para MySQL
	@Column(name = "id_ingredientes") // Debe coincidir con el nombre de la columna en la tabla
	private int id;

	@Column(name = "nombre", nullable = false)
	private String nombre;

	@Column(name = "cantidad_disponible", nullable = false)
	private double cantidadDisponible;

	@Column(name = "costo_por_unidad", nullable = false)
	private double costoPorUnidad;

	@OneToMany(mappedBy = "ingrediente") // Relación con RecetaIngrediente
	private List<RecetaIngrediente> recetaIngredientes;

	// Constructor completo
	public Ingrediente(int id, String nombre, double cantidadDisponible, double costoPorUnidad) {
		this.id = id;
		this.nombre = nombre;
		this.cantidadDisponible = cantidadDisponible;
		this.costoPorUnidad = costoPorUnidad;
	}

	// Constructor vacío
	public Ingrediente() {
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

	public double getCantidadDisponible() {
		return cantidadDisponible;
	}

	public void setCantidadDisponible(double cantidadDisponible) {
		this.cantidadDisponible = cantidadDisponible;
	}

	public double getCostoPorUnidad() {
		return costoPorUnidad;
	}

	public void setCostoPorUnidad(double costoPorUnidad) {
		this.costoPorUnidad = costoPorUnidad;
	}

	public List<RecetaIngrediente> getRecetaIngredientes() {
		return recetaIngredientes;
	}

	public void setRecetaIngredientes(List<RecetaIngrediente> recetaIngredientes) {
		this.recetaIngredientes = recetaIngredientes;
	}
}
