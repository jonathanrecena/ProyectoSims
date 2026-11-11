import javax.swing.*;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conexion {

    public static Connection getConexion() {
        Connection con = null;

        // Leemos el archivo de propiedades que Maven empaquetará dentro del JAR
        try (InputStream input = Conexion.class.getClassLoader().getResourceAsStream("database.properties")) {

            if (input == null) {
                JOptionPane.showMessageDialog(null,
                        "Error crítico: No se encontró el archivo de configuración de la BD.",
                        "Error de Configuración",
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }

            Properties prop = new Properties();
            prop.load(input);

            String url = "jdbc:postgresql://" + prop.getProperty("host") + ":" + prop.getProperty("port") + "/" + prop.getProperty("bd") + "?sslmode=require";

            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection(url, prop.getProperty("user"), prop.getProperty("pass"));

        } catch (ClassNotFoundException e) {
            System.err.println("Error crítico: No se encontró el driver JDBC de PostgreSQL - " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error crítico: No se pudo conectar a la base de datos.\n" + e.getMessage(),
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE);
        }
        return con;
    }
}