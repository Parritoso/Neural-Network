# Instalar el paquete pheatmap si no está instalado
if(!requireNamespace("pheatmap", quietly = TRUE)){
  install.packages("pheatmap")
}

# Cargar la librería pheatmap
library(pheatmap)

# Asegurarse de que la variable auc_values existe y tiene datos
if (!exists("auc_values") || length(auc_values) == 0) {
  stop("Error: La variable auc_values no se ha definido o está vacía.")
}

# Asegurarse de que la variable num_labels existe y es un número positivo
if (!exists("num_labels") || !is.numeric(num_labels) || num_labels <= 0) {
  stop("Error: La variable num_labels no se ha definido o no es válida.")
}

# Asegurarse de que la variable file_path existe y es una cadena de texto
if (!exists("file_path") || !is.character(file_path)) {
  stop("Error: La variable file_path no se ha definido o no es una ruta válida.")
}

# Crear una matriz con los valores AUC
auc_matrix <- matrix(auc_values, nrow = 1, ncol = num_labels)
print(auc_matrix)

# Crear nombres de columnas (opcional)
colnames(auc_matrix) <- paste0("Etiqueta_", 0:(num_labels-1))
# Generar el mapa de calor y guardarlo en un archivo
tryCatch({
  png(filename = file_path, width = 3840, height = 2160)
  pheatmap(auc_matrix,
           color = colorRampPalette(c("blue", "white", "red"))(100),
           display_numbers = TRUE,
           number_format = "%.1e",
           cluster_rows = FALSE,
           cluster_cols = FALSE,
           main = "Mapa de Calor AUC por Etiqueta")
  dev.off()
  cat(paste0("Mapa de calor AUC guardado correctamente en: ", file_path, "\n"))
}, error = function(e) {
  cat(paste0("Error al guardar el mapa de calor: ", e$message, "\n"))
})