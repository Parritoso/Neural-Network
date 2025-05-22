library(dplyr)
library(tidyr)

# 1. Separar las etiquetas GO si hay múltiples en una celda
go_terms_separados <- preprocessed_dataset_adjusted %>%
  dplyr::select(Gene, GO_Term) %>%
  separate_longer_delim(GO_Term, delim = "; ")

# 2. Obtener todas las etiquetas GO únicas
etiquetas_go_unicas <- unique(go_terms_separados$GO_Term)
num_etiquetas <- length(etiquetas_go_unicas)
cat(paste("Número total de etiquetas GO únicas encontradas:", num_etiquetas, "\n"))

# 3. Crear un dataframe para las etiquetas GO (inicialmente con ceros)
df_etiquetas_go <- data.frame(matrix(0, nrow = nrow(preprocessed_dataset_adjusted), ncol = num_etiquetas))
colnames(df_etiquetas_go) <- etiquetas_go_unicas

# 4. Llenar el dataframe de etiquetas GO con 1 si el gen está asociado a la etiqueta
for (i in 1:nrow(preprocessed_dataset_adjusted)) {
  gene_actual <- preprocessed_dataset_adjusted$Gene[i]
  go_terms_gen <- unlist(strsplit(preprocessed_dataset_adjusted$GO_Term[i], "; "))
  indices_etiquetas <- which(etiquetas_go_unicas %in% go_terms_gen)
  if (length(indices_etiquetas) > 0) {
    df_etiquetas_go[i, indices_etiquetas] <- 1
  }
}

# 5. Seleccionar las 10 columnas de expresión (tus features X)
features_x <- preprocessed_dataset_adjusted %>%
  dplyr::select(starts_with("EX"))

# 6. Combinar las features X con las etiquetas GO (Y)
dataset_xy <- cbind(features_x, df_etiquetas_go)

cat("Dataset en formato X (features de expresión) -> Y (etiquetas GO) creado.\n")
# Puedes ver las dimensiones del nuevo dataset
cat(paste("Dimensiones del dataset X -> Y:", nrow(dataset_xy), "filas y", ncol(dataset_xy), "columnas.\n"))

# *** NUEVO CÓDIGO PARA ORDENAR LAS FILAS ALEATORIAMENTE Y DIVIDIR EL DATASET ***

# 1. Obtener los índices de las filas
indices <- 1:nrow(dataset_xy)

# 2. Reordenar los índices de forma aleatoria
indices_aleatorios <- sample(indices)

# 3. Crear un nuevo dataset con las filas en orden aleatorio
dataset_aleatorio <- dataset_xy[indices_aleatorios, ]

# 4. Calcular el punto de división para 80%-20%
punto_division <- floor(0.8 * nrow(dataset_aleatorio))

# 5. Crear el dataset de entrenamiento
train_dataset <- dataset_aleatorio[1:punto_division, ]

# 6. Crear el dataset de prueba
test_dataset <- dataset_aleatorio[(punto_division + 1):nrow(dataset_aleatorio), ]

cat(paste("Dataset completo reordenado aleatoriamente.\n"))
cat(paste("Tamaño del dataset de entrenamiento (80%):", nrow(train_dataset), "filas.\n"))
cat(paste("Tamaño del dataset de prueba (20%):", nrow(test_dataset), "filas.\n"))

# Si quieres guardar los datasets de entrenamiento y prueba en archivos CSV:
 write.csv(train_dataset, "train_dataset.csv", row.names = TRUE)
 write.csv(test_dataset, "test_dataset.csv", row.names = TRUE)

# Si quieres guardar este dataset a un archivo CSV:
 write.csv(dataset_xy, "dataset_x_y.csv", row.names = FALSE)
 