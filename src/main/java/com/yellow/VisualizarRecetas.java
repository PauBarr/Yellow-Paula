package com.yellow;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// Importaciones para PDFBox
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;


// MODIFICADO: Ahora extiende JPanel
public class VisualizarRecetas extends JPanel {

    private JTable recetasTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton eliminarButton;
    private JButton actualizarCostosButton;
    private JButton verDetalleButton;
    private JButton atrasButton;
    private JButton exportarPDFButton; // Nuevo botón para exportar a PDF

    private JComboBox<Categoria> filtroCategoriaComboBox;
    private JTextField buscadorTextField;

    // AÑADIDO: Referencia a la ventana padre (IngresoReceta)
    private IngresoReceta ventanaPadre;
    private SessionFactory sessionFactory;

    private Categoria categoriaInicialFiltro;

    // MODIFICADO: Nuevo constructor para aceptar IngresoReceta como padre
    public VisualizarRecetas(IngresoReceta ventanaPadre, SessionFactory sessionFactory, Categoria categoriaInicialFiltro) {
        this.ventanaPadre = ventanaPadre; // Guardar la referencia
        this.sessionFactory = sessionFactory;
        this.categoriaInicialFiltro = categoriaInicialFiltro;
        initComponents();
        cargarCategoriasEnFiltro();
        cargarDatosTabla();
        if (this.categoriaInicialFiltro != null) {
            // Seleccionar la categoría en el ComboBox y aplicar el filtro
            // Primero, asegúrate de que el item exista en el ComboBox
            for (int i = 0; i < filtroCategoriaComboBox.getItemCount(); i++) {
                Categoria item = filtroCategoriaComboBox.getItemAt(i);
                if (item != null && item.getIdCategoria() == this.categoriaInicialFiltro.getIdCategoria()) {
                    filtroCategoriaComboBox.setSelectedItem(item);
                    break;
                }
            }
            aplicarFiltro();
        }
    }

    // Constructor original (para cuando no hay filtro inicial), llama al nuevo constructor
    public VisualizarRecetas(IngresoReceta ventanaPadre, SessionFactory sessionFactory) {
        this(ventanaPadre, sessionFactory, null);
    }

    private void initComponents() {
        // ELIMINADO: setTitle("Visualizar y Gestionar Recetas");
        // ELIMINADO: setSize(1000, 700);
        // ELIMINADO: setLocationRelativeTo(null);
        // ELIMINADO: setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // MODIFICADO: Configuración del panel en sí
        setLayout(new BorderLayout());
        setBackground(new Color(255, 255, 220));

        JLabel tituloLabel = new JLabel("LISTA DE RECETAS", SwingConstants.CENTER);
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(tituloLabel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Nombre Receta", "Descripción", "Costo Total", "Fecha Creación", "Categorías"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recetasTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        recetasTable.setRowSorter(sorter);
        JScrollPane scrollPane = new JScrollPane(recetasTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel filtroPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filtroPanel.setBackground(new Color(255, 255, 220));

        filtroPanel.add(new JLabel("Filtrar por Categoría:"));
        filtroCategoriaComboBox = new JComboBox<>();
        filtroCategoriaComboBox.setPreferredSize(new Dimension(150, 25));
        filtroCategoriaComboBox.addActionListener(e -> aplicarFiltro());
        filtroPanel.add(filtroCategoriaComboBox);

        filtroPanel.add(new JLabel("Buscar por Nombre:"));
        buscadorTextField = new JTextField(20);
        buscadorTextField.setPreferredSize(new Dimension(150, 25));
        buscadorTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { aplicarFiltro(); }
            @Override
            public void removeUpdate(DocumentEvent e) { aplicarFiltro(); }
            @Override
            public void changedUpdate(DocumentEvent e) { aplicarFiltro(); }
        });
        filtroPanel.add(buscadorTextField);

        add(filtroPanel, BorderLayout.PAGE_START);

        JPanel buttonPanel = new JPanel();
        eliminarButton = new JButton("ELIMINAR RECETA SELECCIONADA");
        actualizarCostosButton = new JButton("ACTUALIZAR COSTOS DE RECETA");
        verDetalleButton = new JButton("VER DETALLE DE RECETA");
        exportarPDFButton = new JButton("EXPORTAR A PDF"); // Inicialización del nuevo botón
        atrasButton = new JButton("ATRÁS");

        buttonPanel.add(eliminarButton);
        buttonPanel.add(actualizarCostosButton);
        buttonPanel.add(verDetalleButton);
        buttonPanel.add(exportarPDFButton); // Añadir el nuevo botón al panel
        buttonPanel.add(atrasButton);
        add(buttonPanel, BorderLayout.SOUTH);

        eliminarButton.addActionListener(e -> eliminarRecetaSeleccionada());
        actualizarCostosButton.addActionListener(e -> actualizarCostoRecetaSeleccionada());
        verDetalleButton.addActionListener(e -> verDetalleReceta());
        exportarPDFButton.addActionListener(e -> exportarRecetasAPDF()); // Asignar acción al nuevo botón
        // MODIFICADO: Llama al método de la ventana padre para cambiar de panel
        atrasButton.addActionListener(e -> ventanaPadre.mostrarPanel("principal"));
    }

    private void cargarCategoriasEnFiltro() {
        filtroCategoriaComboBox.removeAllItems();
        filtroCategoriaComboBox.addItem(null);
        try (Session session = sessionFactory.openSession()) {
            List<Categoria> categorias = session.createQuery("FROM Categoria", Categoria.class).list();
            for (Categoria categoria : categorias) {
                filtroCategoriaComboBox.addItem(categoria);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar categorías en el filtro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void cargarDatosTabla() {
        tableModel.setRowCount(0);
        try (Session session = sessionFactory.openSession()) {
            List<Receta> recetas = session.createQuery("SELECT DISTINCT r FROM Receta r LEFT JOIN FETCH r.categorias", Receta.class).list();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            for (Receta receta : recetas) {
                String categoriasStr = receta.getCategorias().stream()
                                            .map(Categoria::getNombreCategoria)
                                            .reduce((a, b) -> a + ", " + b)
                                            .orElse("Sin categoría");
                Object[] rowData = {
                    receta.getId(),
                    receta.getNombre(),
                    receta.getDescripcion(),
                    String.format("$%.2f", receta.getCostoTotal()),
                    sdf.format(receta.getFechaCreacion()),
                    categoriasStr
                };
                tableModel.addRow(rowData);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar recetas en la tabla: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void aplicarFiltro() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        Categoria categoriaSeleccionada = (Categoria) filtroCategoriaComboBox.getSelectedItem();
        if (categoriaSeleccionada != null) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    String categoriasCelda = (String) entry.getValue(5);
                    if (categoriasCelda == null || categoriasCelda.trim().isEmpty()) {
                        return false;
                    }
                    String[] nombresCategoriasEnCelda = categoriasCelda.split(",\\s*");
                    String nombreCategoriaFiltro = categoriaSeleccionada.getNombreCategoria();

                    for (String nombreCat : nombresCategoriasEnCelda) {
                        if (nombreCat.equalsIgnoreCase(nombreCategoriaFiltro)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        String textoBusqueda = buscadorTextField.getText().trim();
        if (!textoBusqueda.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + textoBusqueda, 1));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }


    private void eliminarRecetaSeleccionada() {
        int selectedRowView = recetasTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una receta para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedModelRow = recetasTable.convertRowIndexToModel(selectedRowView);
        Integer recetaId = (Integer) tableModel.getValueAt(selectedModelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar la receta seleccionada?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Session session = null;
            Transaction transaction = null;
            try {
                session = sessionFactory.openSession();
                transaction = session.beginTransaction();

                Receta receta = session.get(Receta.class, recetaId);
                if (receta != null) {
                    session.delete(receta);
                    transaction.commit();
                    JOptionPane.showMessageDialog(this, "Receta eliminada exitosamente.");
                    cargarDatosTabla();
                    aplicarFiltro();
                } else {
                    JOptionPane.showMessageDialog(this, "Receta no encontrada.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                if (transaction != null) {
                    transaction.rollback();
                }
                JOptionPane.showMessageDialog(this, "Error al eliminar la receta: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                if (session != null) {
                    session.close();
                }
            }
        }
    }

    private void actualizarCostoRecetaSeleccionada() {
        int selectedRowView = recetasTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una receta para actualizar su costo.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedModelRow = recetasTable.convertRowIndexToModel(selectedRowView);
        Integer recetaId = (Integer) tableModel.getValueAt(selectedModelRow, 0);

        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            Receta receta = session.get(Receta.class, recetaId);
            if (receta != null) {
                for (RecetaIngrediente ri : receta.getRecetaIngredientes()) {
                    session.refresh(ri.getIngrediente());
                }

                receta.recalcularCostoTotal();
                session.merge(receta);
                transaction.commit();
                JOptionPane.showMessageDialog(this, "Costo de la receta actualizado exitosamente.");
                cargarDatosTabla();
                aplicarFiltro();
            } else {
                JOptionPane.showMessageDialog(this, "Receta no encontrada.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            JOptionPane.showMessageDialog(this, "Error al actualizar el costo de la receta: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void verDetalleReceta() {
        int selectedRowView = recetasTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una receta para ver su detalle.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedModelRow = recetasTable.convertRowIndexToModel(selectedRowView);
        Integer recetaId = (Integer) tableModel.getValueAt(selectedModelRow, 0);

        try (Session session = sessionFactory.openSession()) {
            Receta receta = session.createQuery(
                "SELECT r FROM Receta r " +
                "LEFT JOIN FETCH r.recetaIngredientes ri " +
                "LEFT JOIN FETCH ri.ingrediente " +
                "LEFT JOIN FETCH r.categorias " +
                "WHERE r.id = :id", Receta.class)
                .setParameter("id", recetaId)
                .uniqueResult();

            if (receta != null) {
                StringBuilder detalle = new StringBuilder();
                detalle.append("Nombre: ").append(receta.getNombre()).append("\n");
                detalle.append("Descripción: ").append(receta.getDescripcion()).append("\n");
                detalle.append("Costo Total: ").append(String.format("$%.2f", receta.getCostoTotal())).append("\n");
                detalle.append("Fecha Creación: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(receta.getFechaCreacion())).append("\n");
                detalle.append("Tiempo de Preparación: ").append(receta.getTiempoPreparacion()).append(" minutos").append("\n\n");

                detalle.append("Categorías:\n");
                if (receta.getCategorias() != null && !receta.getCategorias().isEmpty()) {
                    receta.getCategorias().forEach(cat -> detalle.append("  - ").append(cat.getNombreCategoria()).append("\n"));
                } else {
                    detalle.append("  No hay categorías asociadas.\n");
                }
                detalle.append("\n");

                detalle.append("Ingredientes:\n");

                if (receta.getRecetaIngredientes() != null && !receta.getRecetaIngredientes().isEmpty()) {
                    for (RecetaIngrediente ri : receta.getRecetaIngredientes()) {
                        detalle.append("  - ").append(ri.getIngrediente().getNombre())
                               .append(": ").append(ri.getCantidadUtilizada())
                               .append(" ").append(ri.getUnidadUtilizada())
                               .append(" (Costo de Compra: $").append(String.format("%.2f", ri.getIngrediente().getCostoDeCompra()))
                               .append(" por ").append(ri.getIngrediente().getCantidadDeCompra()).append(" ").append(ri.getIngrediente().getTipoPesoLt())
                               .append(", Costo Real en Receta: $").append(String.format("%.2f", ri.getCostoReal()))
                               .append(")\n");
                    }
                } else {
                    detalle.append("  No hay ingredientes asociados a esta receta.\n");
                }

                JTextArea detailArea = new JTextArea(detalle.toString());
                detailArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(detailArea);
                scrollPane.setPreferredSize(new Dimension(550, 450));

                JOptionPane.showMessageDialog(this, scrollPane, "Detalle de Receta: " + receta.getNombre(), JOptionPane.INFORMATION_MESSAGE);

            } else {
                JOptionPane.showMessageDialog(this, "Receta no encontrada.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al obtener el detalle de la receta: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Exporta las recetas actualmente visibles en la tabla (después de aplicar filtros) a un archivo PDF.
     * El PDF incluirá el nombre, descripción, costo total y categorías de cada receta.
     */
    private void exportarRecetasAPDF() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay recetas para exportar.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        PDDocument document = new PDDocument();
        PDPageContentStream contentStream = null; // Declarar fuera del try-with-resources

        try {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page); // Inicializar aquí

            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Listado de Recetas - Yellow App");
            contentStream.endText();

            contentStream.setFont(PDType1Font.HELVETICA, 10);
            float margin = 50;
            float yStart = 720;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float rowHeight = 15;
            float yPosition = yStart;

            // Headers
            String[] headers = {"Nombre", "Costo", "Categorías", "Descripción"};
            // Simplified column widths for PDF
            float[] colWidths = {0.25f, 0.15f, 0.30f, 0.30f};
            float xPosition;

            // Draw Headers
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
            xPosition = margin;
            for (int i = 0; i < headers.length; i++) {
                contentStream.beginText();
                contentStream.newLineAtOffset(xPosition, yPosition);
                contentStream.showText(headers[i]);
                contentStream.endText();
                xPosition += tableWidth * colWidths[i];
            }
            yPosition -= rowHeight;
            contentStream.drawLine(margin, yPosition, margin + tableWidth, yPosition);
            yPosition -= 5; // Small gap after header line

            contentStream.setFont(PDType1Font.HELVETICA, 8);

            // Iterate over visible rows in the JTable
            for (int i = 0; i < recetasTable.getRowCount(); i++) {
                int modelRow = recetasTable.convertRowIndexToModel(i); // Get the model row index

                String nombreReceta = (String) tableModel.getValueAt(modelRow, 1);
                String costoTotal = (String) tableModel.getValueAt(modelRow, 3);
                String categorias = (String) tableModel.getValueAt(modelRow, 5);
                String descripcion = (String) tableModel.getValueAt(modelRow, 2);

                if (yPosition < margin + 50) { // Check if new page is needed
                    contentStream.close(); // Close content stream for current page
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page); // Create new content stream
                    contentStream.setFont(PDType1Font.HELVETICA, 8); // Reset font
                    yPosition = yStart; // Reset y position for new page

                    // Redraw headers on new page
                    xPosition = margin;
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                    for (int j = 0; j < headers.length; j++) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(xPosition, yPosition);
                        contentStream.showText(headers[j]);
                        contentStream.endText();
                        xPosition += tableWidth * colWidths[j];
                    }
                    yPosition -= rowHeight;
                    contentStream.drawLine(margin, yPosition, margin + tableWidth, yPosition);
                    yPosition -= 5; // Small gap after header line
                    contentStream.setFont(PDType1Font.HELVETICA, 8);
                }

                xPosition = margin;
                
                contentStream.beginText();
                contentStream.newLineAtOffset(xPosition, yPosition);
                contentStream.showText(truncateString(nombreReceta, 30));
                xPosition += tableWidth * colWidths[0];

                contentStream.newLineAtOffset(xPosition - (tableWidth * colWidths[0]), 0); // Reset X and then move
                contentStream.showText(costoTotal);
                xPosition += tableWidth * colWidths[1];

                contentStream.newLineAtOffset(xPosition - (tableWidth * colWidths[0] + tableWidth * colWidths[1]), 0); // Reset X and then move
                contentStream.showText(truncateString(categorias, 35));
                xPosition += tableWidth * colWidths[2];

                contentStream.newLineAtOffset(xPosition - (tableWidth * colWidths[0] + tableWidth * colWidths[1] + tableWidth * colWidths[2]), 0); // Reset X and then move
                contentStream.showText(truncateString(descripcion, 35));
                contentStream.endText();
                
                yPosition -= rowHeight; // Move to the next row
                contentStream.drawLine(margin, yPosition, margin + tableWidth, yPosition); // Line separator
                yPosition -= 2; // Small gap after line

            }

            contentStream.close(); // Close the last content stream

            // File Chooser for saving the PDF
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar Lista de Recetas");
            fileChooser.setSelectedFile(new File("listado_recetas.pdf"));
            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
                }
                document.save(fileToSave);
                JOptionPane.showMessageDialog(this, "Lista de recetas guardada en:\n" + fileToSave.getAbsolutePath(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(fileToSave);
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al generar o guardar el PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try {
                if (document != null) {
                    document.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Helper method to truncate strings for PDF display
    private String truncateString(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() > maxLength) {
            return text.substring(0, maxLength - 3) + "...";
        }
        return text;
    }

    // ELIMINADO: irAtras() ya que ahora lo maneja el padre
}