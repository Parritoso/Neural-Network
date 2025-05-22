package com.bioinformatica.preprocessing.integration.EnvironmentChecker;

import com.bioinformatica.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class REnvironmentChecker {
    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isLinux() {
        return OS.contains("nix") || OS.contains("nux") || OS.contains("aix");
    }

    public static boolean isMac() {
        return OS.contains("mac");
    }

    public static boolean isRInstalled() {
        try {
            ProcessBuilder pb = new ProcessBuilder("R", "--version");
            Process process = pb.start();
//            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//            String line;
//            while ((line = errorReader.readLine()) != null) {
//                System.err.println("Error al ejecutar R: " + line);
//            }
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    public static boolean isRserveInstalled() {
        try {
            ProcessBuilder pb = new ProcessBuilder("R", "-e", "library(Rserve)");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    public static boolean isRserveRunning() {
        try {
            ProcessBuilder pb = new ProcessBuilder("R", "-e", "suppressWarnings(try(library(Rserve),TRUE)) && Rserve::run.Rserve(FALSE, port=6311)");
            Process process = pb.start();
            Thread.sleep(2000);
            return true;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    public static void checkAndGuideR(boolean stopIfMissing) {
        if (!isRInstalled()) {
            System.out.println("Error: R no está instalado en su sistema.");
            if (isWindows()) {
                System.out.println("Por favor, visite [https://www.r-project.org/](https://www.r-project.org/) para descargar la versión de Windows e instálela.");
                System.out.println("Después de la instalación, asegúrese de que la ruta al directorio 'bin' de R (por ejemplo, 'C:\\Program Files\\R\\R-x.y.z\\bin') esté añadida a su variable de entorno PATH.");
                System.out.println("Puede hacerlo buscando 'variables de entorno' en el menú de inicio de Windows, seleccionando 'Editar las variables de entorno del sistema', luego 'Variables de entorno' y editando la variable 'Path' en 'Variables del sistema'.");
            } else if (isLinux()) {
                System.out.println("Por favor, utilice el gestor de paquetes de su distribución para instalar R (e.g., 'sudo apt-get install r-base').");
                System.out.println("Generalmente, el instalador del paquete añade automáticamente R al PATH. Si tiene problemas, asegúrese de que el directorio donde se encuentra el ejecutable 'R' esté en su PATH. Puede verificarlo con el comando 'which R' en la terminal.");
                System.out.println("Para añadirlo al PATH, puede editar su archivo de configuración de shell (como '.bashrc' o '.zshrc') añadiendo una línea como 'export PATH=\"/usr/bin:$PATH\"' (reemplazando '/usr/bin' con la ruta correcta si es diferente) y luego ejecutar 'source ~/.bashrc' o 'source ~/.zshrc'.");
            } else if (isMac()) {
                System.out.println("Por favor, visite [https://www.r-project.org/](https://www.r-project.org/) para descargar la versión de macOS e instálela.");
                System.out.println("El instalador de macOS suele añadir R al PATH. Si tiene problemas, puede que necesite añadir la ruta '/usr/local/bin' a su PATH. Puede hacerlo editando su archivo de configuración de shell (como '.bashrc' o '.zshrc') añadiendo una línea como 'export PATH=\"/usr/local/bin:$PATH\"' y luego ejecutando 'source ~/.bashrc' o 'source ~/.zshrc'.");
            } else {
                System.out.println("Por favor, visite [https://www.r-project.org/](https://www.r-project.org/) para obtener instrucciones de instalación y configuración del PATH para su sistema operativo.");
            }
            if (stopIfMissing) {
                System.out.println("La aplicación no puede continuar sin R. Saliendo.");
                System.exit(1);
            }
            Main.R = false;
        } else {
            System.out.println("R está instalado.");
        }
    }

    public static void checkAndGuideRserve(boolean stopIfMissing) {
        boolean rserveInstalled = isRserveInstalled();
        boolean rserveRunning = isRserveRunning();

        if (!rserveInstalled) {
            System.out.println("Error: La librería Rserve no está instalada en R.");
            System.out.println("Por favor, abra R y ejecute el comando: install.packages('Rserve')");
            if (stopIfMissing) {
                System.out.println("La aplicación no puede continuar sin la librería Rserve. Saliendo.");
                System.exit(1); // Código de salida indicando un error
            }
            Main.R = false;
        } else {
            System.out.println("La librería Rserve está instalada en R.");
            if (!rserveRunning) {
                System.out.println("Advertencia: Rserve no está corriendo.");
                System.out.println("Por favor, abra R y ejecute el comando: library(Rserve); Rserve()");
                // No necesariamente detener aquí, ya que el usuario podría iniciarlo después
                Main.R = false;
            } else {
                System.out.println("Rserve está corriendo.");
            }
        }
    }

    public static void checkEnvironment(boolean stopIfMissing) {
        System.out.println("--- Verificando Entorno R ---");
        checkAndGuideR(stopIfMissing);
        checkAndGuideRserve(stopIfMissing);
        System.out.println("---------------------------");
    }
}
