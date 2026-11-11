# 🎮 Los Sims 2: Memory Game

Minijuego de escritorio estilo **Memory** inspirado en la mítica saga de *Los Sims 2*.

💚 **Un homenaje a los fans de la saga:** Creado especialmente para aquellos jugadores fieles que esperaban pacientemente a que se instalara *Los Sims 2* en su ordenador y amenizaban los minutos jugando al nostálgico minijuego de parejas que aparecía en el instalador. Este proyecto está directamente inspirado en ese recuerdo.

Desarrollado en Java con interfaz gráfica Swing y un sistema de **ranking global en tiempo real** conectado a una base de datos PostgreSQL alojada en Supabase.
---

## 📸 Características principales

* **Interfaz Personalizada:** Diseñada con Java Swing utilizando la tipografía oficial de Los Sims y una paleta de colores pastel.
* **Sistema de Dificultad:** Modos de juego de 8 parejas (4x4) y 12 parejas (4x6).
* **Ranking Global:** Registra los mejores tiempos y puntuaciones máximas de todos los jugadores conectados.
* **Carrusel de Curiosidades:** Panel lateral con rotación automática temporizada de frases e historia de la saga.
* **Persistencia Segura:** Integración JDBC con gestión de credenciales aisladas del control de versiones.
* **Multiplataforma:** Distribuido como ejecutable nativo de Windows (`.exe`) y binario ejecutable universal (`.jar`).

---
## 📁 Estructura del proyecto

```text
src/
└── main/
    ├── java/
    │   ├── Main.java                 # Punto de entrada de la aplicación
    │   ├── VentanaInicioArcade.java  # Menú principal y visualización de ranking
    │   ├── VentanaJuego.java         # Lógica del Memory, temporizador y tableros
    │   └── Conexion.java             # Gestor de conexiones JDBC con la BBDD
    └── resources/
        ├── fuentes/                  # Fuentes personalizadas (.otf)
        ├── imagenes/                 # Assets gráficos y cartas del juego
        └── database.properties       # Configuración local de acceso a la BBDD
```

---

## 🛠️ Tecnologías utilizadas

* **Lenguaje:** Java 17+
* **Interfaz Gráfica:** Java Swing (AWT / GridBagLayout / Form Layouts)
* **Gestión de Proyecto y Dependencias:** Gradle (Kotlin DSL)
* **Base de Datos:** PostgreSQL / Supabase
* **Persistencia:** JDBC (`PreparedStatement`, `ResultSet`, conexiones seguras)
* **Empaquetado:** Launch4j (conversión a `.exe`)

---

## 🚀 Cómo jugar

### Opción 1: Windows (Recomendada)
1. Ve a la sección de **[Releases](../../releases)** a la derecha del repositorio.
2. Descarga el archivo **`Sims2Memory.exe`**.
3. Haz doble clic y ¡a jugar!

### Opción 2: macOS / Linux / Windows (Vía JAR)
1. Asegúrate de tener instalado Java 17 o superior.
2. Descarga **`ProyectoSims-1.0-SNAPSHOT.jar`** desde la sección **[Releases](../../releases)**.
3. Ejecútalo mediante doble clic o abre la terminal en esa carpeta y ejecuta:
   ```bash
   java -jar ProyectoSims-1.0-SNAPSHOT.jar

