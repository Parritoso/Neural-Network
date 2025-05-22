package com.bioinformatica.function_prediction.Neural_network.CudaChecker;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.bytedeco.cuda.global.cudart.cudaGetDeviceCount;
import static org.bytedeco.cuda.global.cudart.cudaSuccess;

public class CudaChecker {
    // Método principal para comprobar y guiar sobre CUDA
    public static boolean checkCUDAAvailability() {
        if (isHeadless()) {
            System.out.println("Entorno sin GUI detectado. No se puede abrir el navegador.");
            return false;
        }

        try {
            int[] deviceCount = new int[1];
            int result = cudaGetDeviceCount(deviceCount);

            if (result == cudaSuccess && deviceCount[0] > 0) {
                System.out.println("CUDA está disponible. GPUs detectadas: " + deviceCount[0]);
                System.out.println("CUDA está disponible y las DLLs están cargadas.");
                return true; // CUDA está disponible
            } else {
                System.out.println("CUDA no está disponible o no se encontraron GPUs válidas. Usando CPU.");
                return false; // CUDA no está disponible
            }

        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            System.err.println("No se encontraron las bibliotecas de CUDA o hay error al cargarlas.");
            CUDAInstallerHelper.detectSystemDetails();
            downloadCUDAInstaller();
            return false; // Error al cargar CUDA, usamos CPU
        }
    }

    // Intenta abrir el navegador con el instalador de CUDA adecuado
    public static void downloadCUDAInstaller() {
        String url = "https://developer.nvidia.com/cuda-11-6-0-download-archive";
        String os = System.getProperty("os.name").toLowerCase();

        System.out.println("Necesitas instalar CUDA manualmente.");
        System.out.println("Enlace: " + url);

        try {
            if (os.contains("win")) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(url));
                }
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
                String[] browsers = {"xdg-open", "gnome-open", "kde-open"};
                boolean opened = false;

                for (String browser : browsers) {
                    try {
                        Process p = Runtime.getRuntime().exec(new String[]{browser, url});
                        p.waitFor();
                        opened = true;
                        break;
                    } catch (Exception ignored) {}
                }

                if (!opened) {
                    System.out.println("No se pudo abrir automáticamente. Copia y pega el enlace en tu navegador.");
                }
            } else {
                System.out.println("Sistema operativo desconocido. Copia el enlace manualmente.");
            }
        } catch (IOException | URISyntaxException e) {
            System.err.println("Error al intentar abrir el navegador: " + e.getMessage());
        }
    }

    // Devuelve true si estamos en un entorno sin GUI (headless)
    public static boolean isHeadless() {
        return GraphicsEnvironment.isHeadless();
    }
}
