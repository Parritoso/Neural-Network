package com.bioinformatica.function_prediction.Neural_network.CudaChecker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;

public class CUDAInstallerHelper {
    public static void detectSystemDetails() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);

        if (os.contains("win")) {
            detectWindowsDetails();
        } else if (os.contains("nux") || os.contains("nix")) {
            detectLinuxVersionAndArch();
        } else {
            System.out.println("Sistema operativo no soportado.");
        }
    }

    private static void detectWindowsDetails() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", "wmic os get Caption"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            System.out.println("Detección de Windows:");
            while ((line = reader.readLine()) != null) {
                if (line.contains("Windows")) {
                    if (line.contains("Server")) {
                        System.out.println("Tipo: Windows Server");
                    } else {
                        System.out.println("Tipo: Windows Desktop");
                    }
                    System.out.println("Versión detectada: " + line.trim());
                }
            }
        } catch (Exception e) {
            System.out.println("Error detectando versión de Windows.");
        }
    }

    public static void detectLinuxVersionAndArch() {
        String arch = getCommandOutput("uname -m").trim();
        System.out.println("Arquitectura detectada: " + arch);

        if (arch.equals("aarch64")) {
            detectARMVariant();
        } else if (arch.equals("x86_64") || arch.equals("ppc64le")) {
            detectLinuxDistroAndVersion(arch);
        } else {
            System.out.println("Arquitectura no soportada para instalación de CUDA.");
        }
    }

    public static void detectARMVariant() {
        String crossBuild = getCommandOutput("uname -a").toLowerCase();
        boolean isCross = crossBuild.contains("cross");

        String buildType = isCross ? "Cross" : "Native";
        System.out.println("Compilación ARM64-SBSA detectada: " + buildType);

        detectLinuxDistroAndVersion("arm64-sbsa", buildType);
    }

    public static void detectLinuxDistroAndVersion(String arch) {
        detectLinuxDistroAndVersion(arch, "");
    }

    public static void detectLinuxDistroAndVersion(String arch, String armMode) {
        String distroInfo = getCommandOutput("lsb_release -a");
        if (distroInfo.isEmpty()) {
            distroInfo = getCommandOutput("cat /etc/os-release");
        }

        distroInfo = distroInfo.toLowerCase();

        System.out.println("Distribución detectada:");
        if (distroInfo.contains("ubuntu")) {
            System.out.println("Ubuntu");
            if (distroInfo.contains("18.04")) System.out.println("Versión: 18.04");
            else if (distroInfo.contains("20.04")) System.out.println("Versión: 20.04");
            else System.out.println("Versión no estándar de Ubuntu");
        } else if (distroInfo.contains("centos")) {
            System.out.println("CentOS");
            if (distroInfo.contains("7")) System.out.println("Versión: 7");
            else if (distroInfo.contains("8")) System.out.println("Versión: 8");
        } else if (distroInfo.contains("rhel")) {
            System.out.println("RHEL");
            if (distroInfo.contains("7")) System.out.println("Versión: 7");
            else if (distroInfo.contains("8")) System.out.println("Versión: 8");
        } else if (distroInfo.contains("sles")) {
            System.out.println("SLES (SUSE Linux Enterprise Server)");
        } else if (distroInfo.contains("opensuse")) {
            System.out.println("OpenSUSE");
        } else if (distroInfo.contains("debian")) {
            System.out.println("Debian");
        } else if (distroInfo.contains("fedora")) {
            System.out.println("Fedora");
        } else if (distroInfo.contains("microsoft") || distroInfo.contains("wsl")) {
            System.out.println("WSL-Ubuntu");
        } else {
            System.out.println("Distribución no reconocida.");
        }

        if (!armMode.isEmpty()) {
            System.out.println("Build: " + armMode);
        }

        System.out.println("Arquitectura: " + arch);
    }

    private static String getCommandOutput(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command.split(" "));
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            process.waitFor();
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
