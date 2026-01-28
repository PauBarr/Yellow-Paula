package com.yellow;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.text.SimpleDateFormat;

public class VisualizarRecetas extends JPanel {
    private JTable recetasTable;
    private DefaultTableModel tableModel;
    private SessionFactory sessionFactory;
    private IngresoReceta ventanaPadre;
    private Categoria categoriaFiltro;

    /**
     * CONSTRUCTOR DE 3 PARÁMETROS
     * Este es el que arregla la línea roja en IngresoReceta.java
     */
    public VisualizarRecetas(IngresoReceta ventanaPadre, SessionFactory sessionFactory, Categoria categoriaFiltro) {
        this.ventanaPadre = ventanaPadre;
        this.sessionFactory = sessionFactory;
        this.categoriaFiltro = categoriaFiltro;
        initComponents();
        cargarDatosTabla();
    }

    /**
     * CONSTRUCTOR DE 2 PARÁMETROS
     * Por si se llama a la pantalla sin un filtro de categoría previo.
     */
    public VisualizarRecetas(IngresoReceta ventanaPadre, SessionFactory sessionFactory) {
        this(ventanaPadre, sessionFactory, null);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(255, 255, 220));
        
        JLabel titulo = new JLabel("LISTA DE RECETAS", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        add(titulo, BorderLayout.NORTH);

        // Columnas de la tabla
        String[] cols = {"ID", "Nombre", "Descripción", "Costo Total", "Fecha Creación"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override 
            public boolean isCellEditable(int r, int c) { return false; }
        };
        
        recetasTable = new JTable(tableModel);
        add(new JScrollPane(recetasTable), BorderLayout.CENTER);

        // Botón para volver al menú
        JButton btnAtras = new JButton("ATRÁS");
        btnAtras.addActionListener(e -> ventanaPadre.mostrarPanel("principal"));
        
        JPanel pBoton = new JPanel();
        pBoton.add(btnAtras);
        add(pBoton, BorderLayout.SOUTH);
    }

    private void cargarDatosTabla() {
        tableModel.setRowCount(0);
        try (Session s = sessionFactory.openSession()) {
            // Consulta que trae las recetas (puedes filtrar por categoriaFiltro si no es null)
            String hql = "FROM Receta";
            if (categoriaFiltro != null) {
                hql = "SELECT r FROM Receta r JOIN r.categorias c WHERE c.id = " + categoriaFiltro.getId();
            }
            
            List<Receta> recetas = s.createQuery(hql, Receta.class).list();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            for (Receta r : recetas) {
                // Validación de nulos para que no explote si el costo es null
                double costoFinal = (r.getCostoTotal() != null) ? r.getCostoTotal() : 0.0;
                
                Object[] row = { 
                    r.getId(), 
                    r.getNombre(), 
                    (r.getDescripcion() != null) ? r.getDescripcion() : "", 
                    String.format("$%.2f", costoFinal), 
                    (r.getFechaCreacion() != null) ? sdf.format(r.getFechaCreacion()) : "-" 
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }
}