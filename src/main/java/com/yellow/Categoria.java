package com.yellow;

public class Categoria {
	private int idCategoria;
	private String nombreCategoria;

	// Constructor
	public Categoria(int idCategoria, String nombreCategoria) {
		this.idCategoria = idCategoria;
		this.nombreCategoria = nombreCategoria;
	}

	public Categoria() {
	}

	// Getters y setters
	public int getIdCategoria() {
		return idCategoria;
	}

	public void setIdCategoria(int idCategoria) {
		this.idCategoria = idCategoria;
	}

	public String getNombreCategoria() {
		return nombreCategoria;
	}

	public void setNombreCategoria(String nombreCategoria) {
		this.nombreCategoria = nombreCategoria;
	}

	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}
}
