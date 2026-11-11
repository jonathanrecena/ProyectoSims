import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    private static final String HOST = System.getenv("SIMS_DB_HOST");
    private static final String PORT = System.getenv("SIMS_DB_PORT");
    private static final String BD = System.getenv("SIMS_DB_NAME");
    private static final String USER = System.getenv("SIMS_DB_USER");
    private static final String PASS = System.getenv("SIMS_DB_PASS");

    private static final String URL = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + BD + "?sslmode=require";
    public static Connection getConexion() {
        if (PASS.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Aviso de Seguridad: No se ha detectado la contraseña en las VM options.",
                    "Modo Seguro / Offline",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Connection con = null;
        try {
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            System.err.println("Error crítico: No se encontró el driver JDBC de PostgreSQL - " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace(); // Esto mostrará el error detallado en rojo en la consola inferior de IntelliJ
            JOptionPane.showMessageDialog(null,
                    "Error crítico: No se pudo conectar a Supabase.\n" + e.getMessage(),
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE);
        }
        return con;
    }
}