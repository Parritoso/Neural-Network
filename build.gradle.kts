plugins {
    id("java")
}

group = "com.bioinformatica"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // Apache Commons CSV para manejo de archivos CSV
    implementation("org.apache.commons:commons-csv:1.10.0")
    // Apache Commons Math para PCA y cálculos numéricos
    implementation("org.apache.commons:commons-math3:3.6.1")
    // JFreeChart para visualización de datos
    implementation("org.jfree:jfreechart:1.5.3")
    // Jackson para manejo de JSON
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")
    // Dependencia de Rserve para la integración con R
    implementation("org.rosuda.REngine:Rserve:1.8.1")
    // Otras dependencias necesarias
    implementation("org.rosuda.REngine:REngine:2.1.0")
    // Logging con SLF4J y Logback
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.5.17")

    // ND4J con soporte CUDA 11.8 (ajusta según tu versión CUDA)
    implementation("org.nd4j:nd4j-cuda-11.6-platform:1.0.0-M2.1")

    // (Opcional) DL4J core si usarás redes neuronales completas
    implementation("org.deeplearning4j:deeplearning4j-core:1.0.0-M2.1")

    implementation("org.apache.pdfbox:pdfbox:3.0.1")
    implementation("org.apache.pdfbox:pdfbox-tools:3.0.1")

    implementation("org.apache.poi:poi-ooxml:5.2.5")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs = listOf(
            "-Dorg.bytedeco.javacpp.maxphysicalbytes=4096M",
            "-Dorg.bytedeco.javacpp.maxbytes=4096M",
            "-Xmx4G" // límite de heap Java también
    )
}