data_frame <- read.table("dataset_x_y.csv", sep = ",", header = TRUE)
columnas_filtradas <- names(data_frame)[!(names(data_frame) %in% c(paste0("EX", 1:10), ""))]

# Obtener la posición (índice basado en 0) y el nombre de las columnas filtradas
resultado_df <- data.frame(
  Posicion = match(columnas_filtradas,columnas_filtradas) - 1,
  Nombre_Columna = columnas_filtradas
)

# Especificar el nombre del archivo CSV de salida
nombre_archivo_csv <- "../gene_function_map.csv"

# Escribir el data frame resultante en un archivo CSV
write.csv(resultado_df, file = nombre_archivo_csv, row.names = FALSE, col.names = FALSE)

cat(paste0("Se ha creado el archivo '", nombre_archivo_csv, "' con la información de las columnas.\n"))