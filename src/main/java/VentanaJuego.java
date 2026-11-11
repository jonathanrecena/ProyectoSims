import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;

public class VentanaJuego extends JFrame {
    private final VentanaInicioArcade ventanaPadre;
    private final int idJugador;
    private final String nombreJugador;

    // paleta de colores pastel
    private final Color COLOR_SALMON = new Color(249, 168, 145);
    private final Color COLOR_MENTA = new Color(224, 242, 241);
    private final Color VERDE_PASTEL = new Color(212, 236, 203);
    private final Color ROSA_CHICLE = new Color(255,193,204);
    private final Color AZUL_LAVANDA = new Color(213, 228, 252);

    // componentes de control superior
    private JLabel lblCrono;
    private int segundosTranscurridos = 0;
    private Timer timerCrono;

    // configuracion del tablero de cartas (modificado dinámicamente según dificultad)
    private JPanel panelTablero;
    private JButton[][] botonesTablero;
    private int[][] tableroLogico; // guarda el id de la imagen/pareja que tiene asignada cada coordenada
    private final int FILAS = 4;
    private int COLUMNAS;

    // --- variables de estado para la logica del memory
    private JButton primerBotonSeleccionado = null;
    private int filaPrimera = -1, colPrimera = -1;
    private boolean tableroBloqueado = false;
    private int parejasEncontradas = 0;

    // variables para la gestion de textos de la base de datos en la zona izquierda
    private JTextArea txtAreaInfo;
    private ArrayList<String> listaTextosBD;
    private int indiceTextoActual = 0;

    public VentanaJuego(VentanaInicioArcade padre, int idJugador, String nombre, int columnas) {
        this.ventanaPadre = padre;
        this.idJugador = idJugador;
        this.nombreJugador = nombre;
        this.COLUMNAS = columnas;

        setTitle("Sims 2 Memory ¡Jugando!");
        setSize(1050, 800);
        setIconImage(getIconImage());
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(COLOR_SALMON);
        setLocationRelativeTo(padre);

        // Carga inicial de datos de la base de datos para la zona izquierda
        cargarTextosInformativos();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // 1. inicializa la interfaz grafica
        initComponentesJuego();

        // 2. prepara y baraja de forma aleatoria las parejas del tablero
        configurarTableroLogico();

        // 3. arranca el reloj de la cabecera
        configurarCronometro();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gestionarSalida();
            }
        });
        setVisible(true);
    }

    private void cargarTextosInformativos() {
        listaTextosBD = new ArrayList<>();
        String sql = "SELECT texto_frase FROM frases_sims";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                listaTextosBD.add(rs.getString("texto_frase"));
            }
        } catch (SQLException e) {
            listaTextosBD.add("Curiosidad 1: ¡El idioma Simlish nació de la improvisación!");
            listaTextosBD.add("Curiosidad 2: Bella Goth desapareció misteriosamente en Las Rarezas.");
            listaTextosBD.add("Curiosidad 3: Elige bien los deseos de toda la vida de tus Sims.");
        }
        if (listaTextosBD.isEmpty()) {
            listaTextosBD.add("No se encontraron registros de texto en la base de datos.");
        }
    }

    private void initComponentesJuego() {
        //panel del tiempo, arriba
        JPanel panelSuperior = new JPanel(new GridBagLayout());
        panelSuperior.setBackground(COLOR_MENTA);
        panelSuperior.setOpaque(true);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();

        // COLUMNA 0 (IZQUIERDA): Texto de bienvenida del Sim
        JLabel lblInfo = new JLabel("Bienvenido/a Sim: " + nombreJugador, JLabel.LEFT);
        lblInfo.setFont(ventanaPadre.cargarFuenteCustom(24f));
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        panelSuperior.add(lblInfo, gbc);

        // COLUMNA 1 (CENTRO): El objetivo del juego
        JLabel lblInfo1 = new JLabel("Encuentra las " + ((FILAS * COLUMNAS) / 2) + " parejas", JLabel.CENTER);
        lblInfo1.setFont(ventanaPadre.cargarFuenteCustom(24f));
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.CENTER;
        panelSuperior.add(lblInfo1, gbc);

        // COLUMNA 2 (DERECHA): El reloj del juego
        lblCrono = new JLabel("TIEMPO: 00:00", JLabel.RIGHT);
        lblCrono.setFont(ventanaPadre.cargarFuenteCustom(20f));
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.EAST;
        panelSuperior.add(lblCrono, gbc);

        add(panelSuperior, BorderLayout.NORTH);

        // Contenedor intermedio transparente para centrar verticalmente
        JPanel contenedorIzquierdo = new JPanel(new GridBagLayout());
        contenedorIzquierdo.setOpaque(false);
        contenedorIzquierdo.setPreferredSize(new Dimension(280, 0));

        // Panel Lateral Izquierdo: Contenedor interactivo de informacion extra
        JPanel panelIzquierdo = new JPanel(new BorderLayout(10, 10));
        panelIzquierdo.setBackground(AZUL_LAVANDA);
        panelIzquierdo.setPreferredSize(new Dimension(250, 350));
        panelIzquierdo.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        txtAreaInfo = new JTextArea(listaTextosBD.get(indiceTextoActual));
        txtAreaInfo.setFont(new Font("Arial", Font.BOLD, 15));
        txtAreaInfo.setLineWrap(true);
        txtAreaInfo.setWrapStyleWord(true);
        txtAreaInfo.setEditable(false);
        txtAreaInfo.setOpaque(false);

        // Título estático para las curiosidades
        JLabel lblCuriosidades = new JLabel("¿Sabías que...?", SwingConstants.CENTER);
        lblCuriosidades.setFont(new Font("Arial", Font.BOLD, 16));
        panelIzquierdo.add(lblCuriosidades, BorderLayout.NORTH);
        panelIzquierdo.add(new JScrollPane(txtAreaInfo), BorderLayout.CENTER);

        // Temporizador para cambiar la frase automáticamente cada 8 segundos (8000 ms)
        Timer timerFrases = new Timer(8000, e -> {
            if (!listaTextosBD.isEmpty()) {
                indiceTextoActual = (indiceTextoActual + 1) % listaTextosBD.size();
                txtAreaInfo.setText(listaTextosBD.get(indiceTextoActual));
            }
        });
        timerFrases.start();

        GridBagConstraints gbcCentrado = new GridBagConstraints();
        gbcCentrado.gridx = 0;
        gbcCentrado.gridy = 0;
        gbcCentrado.anchor = GridBagConstraints.CENTER;
        contenedorIzquierdo.add(panelIzquierdo, gbcCentrado);

        add(contenedorIzquierdo, BorderLayout.WEST);

        // Panel del tablero
        panelTablero = new JPanel(new GridLayout(FILAS, COLUMNAS, 10, 10));
        panelTablero.setOpaque(false);
        panelTablero.setBorder(BorderFactory.createEmptyBorder(5, 25, 15, 25));

        botonesTablero = new JButton[FILAS][COLUMNAS];
        tableroLogico = new int[FILAS][COLUMNAS];

        for (int f = 0; f < FILAS; f++) {
            for (int c = 0; c < COLUMNAS; c++) {
                botonesTablero[f][c] = new JButton();
                botonesTablero[f][c].setBackground(VERDE_PASTEL);
                botonesTablero[f][c].setFocusable(false);

                final int filaActual = f;
                final int colActual = c;
                botonesTablero[f][c].addActionListener(e -> gestionarClickCarta(filaActual, colActual));

                panelTablero.add(botonesTablero[f][c]);
            }
        }
        add(panelTablero, BorderLayout.CENTER);

        JButton btnAbandonar = new JButton("VOLVER AL MENÚ");
        btnAbandonar.setFont(ventanaPadre.cargarFuenteCustom(22f));
        btnAbandonar.setBackground(Color.LIGHT_GRAY);
        btnAbandonar.setPreferredSize(new Dimension(0, 35));
        btnAbandonar.addActionListener(e -> gestionarSalida());
        add(btnAbandonar, BorderLayout.SOUTH);
    }

    private void configurarTableroLogico() {
        ArrayList<Integer> listaParejas = new ArrayList<>();
        int totalParejas = (FILAS * COLUMNAS) / 2;

        for (int i = 1; i <= totalParejas; i++) {
            listaParejas.add(i);
            listaParejas.add(i);
        }
        Collections.shuffle(listaParejas);

        int indice = 0;
        for (int f = 0; f < FILAS; f++) {
            for (int c = 0; c < COLUMNAS; c++) {
                tableroLogico[f][c] = listaParejas.get(indice);
                indice++;
            }
        }
    }

    private void gestionarClickCarta(int f, int c) {
        if (!tableroBloqueado && botonesTablero[f][c] != primerBotonSeleccionado) {

            int idCarta = tableroLogico[f][c];

            try {
                URL url = ClassLoader.getSystemResource("imagenes/carta" + idCarta + ".png");

                if (url != null) {
                    ImageIcon iconoOriginal = new ImageIcon(url);
                    Image imagenPequenya = iconoOriginal.getImage().getScaledInstance(70, 85, Image.SCALE_SMOOTH);
                    ImageIcon iconoEscalado = new ImageIcon(imagenPequenya);
                    botonesTablero[f][c].setIcon(iconoEscalado);
                } else {
                    botonesTablero[f][c].setText("Sims " + idCarta);
                }
            } catch (Exception ex) {
                botonesTablero[f][c].setText("Sims " + idCarta);
            }

            botonesTablero[f][c].setBackground(ROSA_CHICLE);

            if (primerBotonSeleccionado == null) {
                primerBotonSeleccionado = botonesTablero[f][c];
                filaPrimera = f;
                colPrimera = c;
            } else {
                tableroBloqueado = true;
                int idPrimeraCarta = tableroLogico[filaPrimera][colPrimera];
                if (idCarta == idPrimeraCarta) {
                    Timer timerAcierto = new Timer(400, e -> {
                        botonesTablero[filaPrimera][colPrimera].setVisible(false);
                        botonesTablero[f][c].setVisible(false);
                        parejasEncontradas++;
                        comprobarFinPartida();
                        resetearTurno();
                    });
                    timerAcierto.setRepeats(false);
                    timerAcierto.start();

                } else {
                    Timer timerFallo = new Timer(600, e -> {
                        botonesTablero[filaPrimera][colPrimera].setIcon(null);
                        botonesTablero[filaPrimera][colPrimera].setText("");
                        botonesTablero[filaPrimera][colPrimera].setBackground(VERDE_PASTEL);

                        botonesTablero[f][c].setIcon(null);
                        botonesTablero[f][c].setText("");
                        botonesTablero[f][c].setBackground(VERDE_PASTEL);

                        resetearTurno();
                    });
                    timerFallo.setRepeats(false);
                    timerFallo.start();
                }
            }
        }
    }

    private void resetearTurno() {
        primerBotonSeleccionado = null;
        filaPrimera = -1;
        colPrimera = -1;
        tableroBloqueado = false;
    }

    private void comprobarFinPartida() {
        int totalParejasAMitad = (FILAS * COLUMNAS) / 2;
        if (parejasEncontradas == totalParejasAMitad) {
            timerCrono.stop();
            JOptionPane.showMessageDialog(this,
                    " ¡ENHORABUENA! ¡Has encontrado todas las parejas de Sims! ",
                    "¡Victoria!", JOptionPane.INFORMATION_MESSAGE);
            guardarPartidaEnBBDD();
            cerrarYVolverAInicio();
        }
    }

    private void configurarCronometro() {
        timerCrono = new Timer(1000, e -> {
            segundosTranscurridos++;
            int minutos = segundosTranscurridos / 60;
            int segundos = segundosTranscurridos % 60;
            lblCrono.setText(String.format("TIEMPO: %02d:%02d", minutos, segundos));
        });
        timerCrono.start();
    }

    private void gestionarSalida() {
        timerCrono.stop();

        int seleccion = JOptionPane.showConfirmDialog(
                this,
                "La partida no ha concluido. Si sales ahora, tu progreso se perderá. ¿Deseas salir al menú?",
                "Confirmación de salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (seleccion == JOptionPane.YES_OPTION) {
            cerrarYVolverAInicio();
        } else {
            timerCrono.start();
        }
    }

    public Image getIconImage() {
        try {
            java.net.URL url = ClassLoader.getSystemResource("imagenes/SimsIcono.png");
            if (url != null) {
                return new ImageIcon(url).getImage();
            } else {
                System.err.println("Error: No se encontró el icono en la ruta especificada.");
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void guardarPartidaEnBBDD() {
        int minutos = segundosTranscurridos / 60;
        int segundos = segundosTranscurridos % 60;
        String tiempoFormateado = String.format("%02d:%02d", minutos, segundos);

        int puntuacionSimulada = Math.max(100, 5000 - (segundosTranscurridos * 10));

        try (Connection conn = Conexion.getConexion()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "No hay conexión con la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 1. Insertar el jugador si no existe
            String sqlJugador = "INSERT INTO jugadores (nombre) VALUES (?) ON CONFLICT DO NOTHING";
            try (PreparedStatement psJ = conn.prepareStatement(sqlJugador)) {
                psJ.setString(1, nombreJugador);
                psJ.executeUpdate();
            }

            // 2. Obtener el ID del jugador
            int idJugadorActual = -1;
            String sqlBuscarId = "SELECT id_jugador FROM jugadores WHERE nombre = ?";
            try (PreparedStatement psB = conn.prepareStatement(sqlBuscarId)) {
                psB.setString(1, nombreJugador);
                try (ResultSet rs = psB.executeQuery()) {
                    if (rs.next()) {
                        idJugadorActual = rs.getInt("id_jugador");
                    }
                }
            }

            // 3. Consultar puntuación anterior para el mensaje de récord
            int puntuacionAnterior = -1;
            String sqlBuscarPartida = "SELECT puntuacion FROM partidas WHERE id_jugador = ?";
            try (PreparedStatement psBuscarP = conn.prepareStatement(sqlBuscarPartida)) {
                psBuscarP.setInt(1, idJugadorActual);
                try (ResultSet rs = psBuscarP.executeQuery()) {
                    if (rs.next()) {
                        puntuacionAnterior = rs.getInt("puntuacion");
                    }
                }
            }

            // 4. Guardar o actualizar la partida mediante UPSERT
            String sqlPartida = "INSERT INTO partidas (id_jugador, puntuacion, tiempo_empleado) VALUES (?, ?, ?) " +
                    "ON CONFLICT (id_jugador) DO UPDATE SET " +
                    "  puntuacion = GREATEST(partidas.puntuacion, EXCLUDED.puntuacion), " +
                    "  tiempo_empleado = EXCLUDED.tiempo_empleado";

            try (PreparedStatement psP = conn.prepareStatement(sqlPartida)) {
                psP.setInt(1, idJugadorActual);
                psP.setInt(2, puntuacionSimulada);
                psP.setString(3, tiempoFormateado);
                psP.executeUpdate();
            }

            // 5. Mensajes informativos en pantalla
            if (puntuacionAnterior == -1) {
                JOptionPane.showMessageDialog(this, "¡Partida registrada con éxito para " + nombreJugador + "! Puntos: " + puntuacionSimulada, "Juego Guardado", JOptionPane.INFORMATION_MESSAGE);
            } else if (puntuacionSimulada > puntuacionAnterior) {
                JOptionPane.showMessageDialog(this, "¡¡NUEVO RÉCORD!!\nNueva Puntuación: " + puntuacionSimulada + " pts", "¡RÉCORD!", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Partida guardada. Puntos: " + puntuacionSimulada + " (Récord anterior: " + puntuacionAnterior + " pts)", "Fin Partida", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al conectar con Supabase (PostgreSQL): " + e.getMessage(), "Error BBDD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cerrarYVolverAInicio() {
        this.dispose();
        ventanaPadre.refrescarTablaRanking();
        ventanaPadre.setVisible(true);
    }
}