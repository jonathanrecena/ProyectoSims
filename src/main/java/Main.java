import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaInicioArcade ventana = new VentanaInicioArcade();
            ventana.setVisible(true);
        });
    }
}