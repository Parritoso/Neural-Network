eliminar_primera_columna_csv <- function(ruta_archivo_entrada, ruta_archivo_salida) {
  # Leer el archivo CSV
  datos <- read.csv(ruta_archivo_entrada)

  # Verificar si el archivo tiene al menos una columna
  if (ncol(datos) > 0) {
    # Eliminar la primera columna
    datos_sin_primera_columna <- datos[, -1]

    # Guardar el nuevo dataframe en un nuevo archivo CSV
    write.csv(datos_sin_primera_columna, ruta_archivo_salida, row.names = FALSE)

    cat(paste("Se ha eliminado la primera columna y se ha guardado el resultado en:", ruta_archivo_salida, "\n"))
  } else {
    cat("El archivo CSV está vacío o no tiene columnas.\n")
  }
}

# Ejemplo de uso:
# Especifica la ruta del archivo CSV de entrada
archivo_entrada <- "train_dataset.csv" # Reemplaza con la ruta de tu archivo

# Especifica la ruta donde quieres guardar el nuevo archivo CSV
archivo_salida <- "../train_dataset.csv" # Reemplaza con la ruta deseada

# Llama a la función para realizar la operación
eliminar_primera_columna_csv(archivo_entrada, archivo_salida)

# Nota: Asegúrate de que el archivo CSV de entrada exista en la ruta especificada.
# El nuevo archivo CSV se creará en la ruta especificada.