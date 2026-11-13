import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VentanaInicioArcade extends JFrame {
    private JTextField txtNombreSim;
    private JTable tablaRanking;
    private DefaultTableModel modeloTabla;
    private Font fuenteSims;

    // Paleta de colores pastel del juego
    private final Color COLOR_SALMON = new Color(249, 168, 145);
    private final Color COLOR_MENTA = new Color(224, 242, 241);

    public VentanaInicioArcade() {
        setTitle("Sims 2 Memory - Menú Principal");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Aplicar color de fondo salmón a la ventana principal
        getContentPane().setBackground(COLOR_SALMON);

        // Cargar icono de la ventana de forma segura
        setIconImage(getIconImage());

        // Cargar fuente personalizada desde resources/fuentes/Sims.otf
        cargarFuentePersonalizada();

        // Inicializar componentes visuales
        initComponents();

        // Cargar datos de la base de datos de manera segura
        cargarRanking();
    }

    public Image getIconImage() {
        try {
            java.net.URL url = getClass().getResource("/imagenes/SimsIcono.png");
            if (url != null) {
                return new ImageIcon(url).getImage();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void cargarFuentePersonalizada() {
        try {
            InputStream is = getClass().getResourceAsStream("/fuentes/Sims.otf");
            if (is != null) {
                fuenteSims = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(18f);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fuenteSims);
            } else {
                fuenteSims = new Font("Monospaced", Font.BOLD, 18);
            }
        } catch (Exception e) {
            fuenteSims = new Font("Monospaced", Font.BOLD, 18);
        }
    }

    public Font cargarFuenteCustom(float tamaño) {
        if (fuenteSims != null) {
            return fuenteSims.deriveFont(tamaño);
        }
        return new Font("Monospaced", Font.BOLD, (int) tamaño);
    }

    public void refrescarTablaRanking() {
        cargarRanking();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Panel Superior: Título con fondo Menta
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelSuperior.setBackground(COLOR_MENTA);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitulo = new JLabel("LOS SIMS 2: MEMORY", SwingConstants.CENTER);
        lblTitulo.setFont(cargarFuenteCustom(24f));
        panelSuperior.add(lblTitulo);

        add(panelSuperior, BorderLayout.NORTH);

        // Panel Central: Ingreso de Nombre y Tabla de Ranking
        JPanel panelCentral = new JPanel();
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS)); // Apila elementos verticalmente
        panelCentral.setOpaque(false); // Transparente para ver el fondo salmón

        // Espaciador superior
        panelCentral.add(Box.createRigidArea(new Dimension(0, 20)));

        // --- Zona del Nombre ---
        JPanel panelNombre = new JPanel();
        panelNombre.setOpaque(false);
        JLabel lblNombre = new JLabel("Nombre del Sim:");
        lblNombre.setFont(cargarFuenteCustom(16f));
        panelNombre.add(lblNombre);

        txtNombreSim = new JTextField(15);
        txtNombreSim.setFont(new Font("Arial", Font.PLAIN, 14));
        panelNombre.add(txtNombreSim);

        panelCentral.add(panelNombre);

        // Espaciador entre nombre y tabla
        panelCentral.add(Box.createRigidArea(new Dimension(0, 20)));

        // --- Zona de la Tabla ---
        modeloTabla = new DefaultTableModel(
                new String[]{"Posición", "Sim", "Mejor Puntuación", "Última Partida", "Tiempo Récord"}, 0
        );
        tablaRanking = new JTable(modeloTabla);
        tablaRanking.setFont(new Font("Arial", Font.PLAIN, 12));

        JScrollPane scrollTabla = new JScrollPane(tablaRanking);
        scrollTabla.setPreferredSize(new Dimension(450, 180)); // Tamaño reducido de la tabla
        scrollTabla.setMaximumSize(new Dimension(450, 180)); // Evita que se estire

        JPanel contenedorTabla = new JPanel(); // Contenedor para centrar la tabla
        contenedorTabla.setOpaque(false);
        contenedorTabla.add(scrollTabla);

        panelCentral.add(contenedorTabla);

        add(panelCentral, BorderLayout.CENTER);

        // Panel Inferior: Botón Jugar
        JButton btnJugar = new JButton("¡Jugar Partida!");
        btnJugar.setFont(cargarFuenteCustom(20f));
        btnJugar.addActionListener(e -> {
            String nombre = txtNombreSim.getText().trim();
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, introduce el nombre de tu Sim para jugar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] opcionesDificultad = {"Fácil (4x4 - 8 parejas)", "Normal (4x6 - 12 parejas)"};
            int seleccion = JOptionPane.showOptionDialog(
                    this,
                    "Selecciona la dificultad para tu Sim, " + nombre + ":",
                    "Dificultad del Juego",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opcionesDificultad,
                    opcionesDificultad[0]
            );

            if (seleccion != JOptionPane.CLOSED_OPTION) {
                int columnas = (seleccion == 0) ? 4 : 6;
                int idJugadorSimulado = 1;

                this.setVisible(false);
                new VentanaJuego(this, idJugadorSimulado, nombre, columnas);
            }
        });
        add(btnJugar, BorderLayout.SOUTH);
    }

    public void cargarRanking() {
        modeloTabla.setRowCount(0);

        Connection conn = Conexion.getConexion();
        if (conn == null) {
            return;
        }

        String query = "SELECT j.nombre, p.puntuacion, p.ultima_puntuacion, p.tiempo_empleado " +
                "FROM partidas p " +
                "JOIN jugadores j ON p.id_jugador = j.id_jugador " +
                "ORDER BY p.puntuacion DESC LIMIT 10";

        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            int posicion = 1;
            while (rs.next()) {
                String nombre = rs.getString("nombre");
                int mejorPuntuacion = rs.getInt("puntuacion");
                int ultimaPuntuacion = rs.getInt("ultima_puntuacion");
                String tiempo = rs.getString("tiempo_empleado");
                modeloTabla.addRow(new Object[]{posicion++, nombre, mejorPuntuacion, ultimaPuntuacion, tiempo});
            }

        } catch (SQLException e) {
            System.err.println("Error al ejecutar la consulta del ranking: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaInicioArcade().setVisible(true));
    }
}