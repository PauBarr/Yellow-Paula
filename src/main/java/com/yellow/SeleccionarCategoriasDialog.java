package com.yellow;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // Importado para Collectors

public class SeleccionarCategoriasDialog extends JDialog {

    private SessionFactory sessionFactory;
    private JList<Categoria> listaCategorias; // Nombre en español
    private DefaultListModel<Categoria> modeloLista; // Nombre en español
    private List<Categoria> categoriasSeleccionadas; // Para almacenar las categorías seleccionadas por el usuario
    private boolean confirmado = false; // Para saber si el diálogo fue confirmado o cancelado

    public SeleccionarCategoriasDialog(Frame padre, SessionFactory sessionFactory, List<Categoria> categoriasPreSeleccionadas) { // Nombres en español
        super(padre, "Seleccionar Categorías", true); // Diálogo modal (bloquea la ventana padre)
        this.sessionFactory = sessionFactory;
        // Copiar la lista para evitar modificar la original directamente hasta que se confirme
        this.categoriasSeleccionadas = new ArrayList<>(categoriasPreSeleccionadas);

        initComponents();
        cargarCategorias();
        preseleccionarCategorias();
    }

    private void initComponents() {
        setSize(400, 300);
        setLocationRelativeTo(getParent()); // Centra el diálogo respecto a la ventana padre
        setLayout(new BorderLayout(10, 10)); // Diseño con espacios entre componentes
        JPanel panelPrincipal = new JPanel(new BorderLayout()); // Nombre en español
        panelPrincipal.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelPrincipal.setBackground(new Color(255, 255, 220));

        JLabel etiquetaTitulo = new JLabel("SELECCIONAR CATEGORÍAS", SwingConstants.CENTER); // Nombre en español
        etiquetaTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        panelPrincipal.add(etiquetaTitulo, BorderLayout.NORTH);

        modeloLista = new DefaultListModel<>(); // Nombre en español
        listaCategorias = new JList<>(modeloLista); // Nombre en español
        listaCategorias.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // Permite selección múltiple
        // Renderizador personalizado para mostrar el nombre de la categoría en la lista
        listaCategorias.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Categoria) {
                    setText(((Categoria) value).getNombreCategoria());
                }
                return this;
            }
        });
        JScrollPane panelDesplazamiento = new JScrollPane(listaCategorias); // Nombre en español
        panelPrincipal.add(panelDesplazamiento, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Nombre en español
        panelBotones.setBackground(new Color(255, 255, 220));

        JButton botonAceptar = new JButton("Aceptar"); // Nombre en español
        botonAceptar.setBackground(new Color(85, 107, 47)); // Verde oliva
        botonAceptar.setForeground(Color.WHITE);
        botonAceptar.addActionListener(e -> {
            confirmado = true; // Se confirma que el usuario aceptó
            categoriasSeleccionadas = listaCategorias.getSelectedValuesList(); // Obtiene las categorías seleccionadas
            dispose(); // Cierra el diálogo
        });
        panelBotones.add(botonAceptar);

        JButton botonCancelar = new JButton("Cancelar"); // Nombre en español
        botonCancelar.setBackground(new Color(178, 34, 34)); // Rojo fuego
        botonCancelar.setForeground(Color.WHITE);
        botonCancelar.addActionListener(e -> {
            confirmado = false; // Se marca como cancelado
            dispose(); // Cierra el diálogo
        });
        panelBotones.add(botonCancelar);

        add(panelPrincipal, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void cargarCategorias() {
        modeloLista.clear(); // Limpia la lista antes de cargar
        try (Session session = sessionFactory.openSession()) {
            List<Categoria> categorias = session.createQuery("FROM Categoria", Categoria.class).list();
            for (Categoria categoria : categorias) {
                modeloLista.addElement(categoria); // Añade cada categoría al modelo de la lista
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar las categorías: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void preseleccionarCategorias() {
        // Si hay categorías pre-seleccionadas y la lista no está vacía
        if (!categoriasSeleccionadas.isEmpty() && !modeloLista.isEmpty()) {
            List<Integer> indicesASeleccionar = new ArrayList<>(); // Nombre en español
            // Itera sobre las categorías que deberían estar pre-seleccionadas
            for (Categoria categoriaPreSeleccionada : categoriasSeleccionadas) {
                // Busca el índice de esa categoría en el modelo de la lista
                for (int i = 0; i < modeloLista.size(); i++) {
                    if (modeloLista.getElementAt(i).getIdCategoria() == categoriaPreSeleccionada.getIdCategoria()) {
                        indicesASeleccionar.add(i); // Si la encuentra, añade su índice
                        break; // Pasa a la siguiente categoría pre-seleccionada
                    }
                }
            }
            // Convierte la lista de índices a un array de int y establece la selección
            int[] arrayIndices = indicesASeleccionar.stream().mapToInt(Integer::intValue).toArray(); // Nombre en español
            listaCategorias.setSelectedIndices(arrayIndices);
        }
    }

    // Métodos públicos para que la ventana padre pueda obtener la información
    public List<Categoria> getSelectedCategories() {
        return categoriasSeleccionadas;
    }

    public boolean isConfirmed() {
        return confirmado;
    }
}