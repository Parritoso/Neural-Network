if (!requireNamespace("GO.db", quietly = TRUE)) install.packages("GO.db")
if (!require("BiocManager", quietly = TRUE))
  install.packages("BiocManager")

library(dplyr)
library(tidyr)
library(scales)
library(tibble)
BiocManager::install("GO.db")

preprocessed_dataset <- read.csv("merged_dataset.csv", header=TRUE)

# 1. Copiar el dataset original
preprocessed_dataset_adjusted <- preprocessed_dataset

# 2. Eliminar filas con genes que no hayan sido encontrados o que tengan solo NA en las columnas de expresión
expr_columns <- colnames(preprocessed_dataset)[!(colnames(preprocessed_dataset) %in% c("Gene", "X..GO_ID", "GEO_ID","C8","C9","C10"))]  # Excluir identificadores
preprocessed_dataset <- preprocessed_dataset %>%
  filter(!is.na(Gene) & Gene != "") %>%
  filter(rowSums(!is.na(select(., all_of(expr_columns)))) > 0)  # Al menos una expresión válida

# 3. Reorganizar los valores de expresión para que los NA queden al final de cada fila
preprocessed_dataset_adjusted <- preprocessed_dataset_adjusted %>%
  rowwise() %>%
  mutate(sorted_values = list(sort(c_across(all_of(expr_columns)), na.last = TRUE))) %>%  # Mueve los NA al final
  ungroup()

# 4. Determinar el número mínimo de columnas con valores NO NA en cualquier gen
min_valid_columns <- min(rowSums(!is.na(preprocessed_dataset_adjusted %>% select(all_of(expr_columns)))))

# 5. Crear nuevas columnas con los primeros `min_valid_columns` valores por fila
preprocessed_dataset_adjusted <- preprocessed_dataset_adjusted %>%
  mutate(across(all_of(expr_columns), ~NULL)) %>%  # Eliminar columnas antiguas
  unnest_wider(sorted_values, names_sep = "_")  # Expandir la lista en nuevas columnas

# 6. Renombrar las nuevas columnas de expresión de manera uniforme
expr_new_names <- paste0("EX", seq_len(min_valid_columns))  # Genera nombres EX1, EX2, ..., EXn
# Identificar el índice donde comienzan las columnas de expresión (después de Gene, C8, C9, C10)
expr_start_index <- which(colnames(preprocessed_dataset_adjusted) == "GEO_ID") + 1
expr_end_index <- expr_start_index + min_valid_columns - 1

# Aplicar los nuevos nombres solo a las columnas de expresión
colnames(preprocessed_dataset_adjusted)[expr_start_index:expr_end_index] <- expr_new_names

# *** NUEVO CÓDIGO PARA OBTENER LAS ETIQUETAS GO ***
# Primero, vamos a extraer todos los códigos GO únicos de tu dataset.
# Asumo que los códigos GO están en la primera columna. Ajusta el índice si no es así.
go_codes <- unique(preprocessed_dataset_adjusted[[2]])

# Luego, vamos a buscar las descripciones (etiquetas) para cada código GO.
# La función Term() de la librería GO.db nos ayuda con esto.
go_terms <- Term(go_codes)

# Ahora, vamos a crear un data frame que relacione cada código GO con su etiqueta.
go_annotations <- data.frame(
  GO_ID = names(go_terms),
  GO_Term = as.character(go_terms)
)

# Finalmente, vamos a combinar este data frame con tu dataset original
# usando el código GO como clave para unir las tablas.
preprocessed_dataset_adjusted <- left_join(preprocessed_dataset_adjusted, go_annotations, by = c("X..GO_ID" = "GO_ID"))

# *** FIN DEL NUEVO CÓDIGO ***

# 7. Seleccionar solo las columnas necesarias (Gene + nuevas columnas de expresión)
class(preprocessed_dataset_adjusted)
preprocessed_dataset_adjusted <- preprocessed_dataset_adjusted %>%
  dplyr::select(Gene, GO_Term, X..GO_ID, C8, C9, C10, GEO_ID, all_of(expr_new_names))

# 8. Aplicar Min-Max Scaling a las columnas de expresión
preprocessed_dataset_adjusted[, expr_new_names] <- as.data.frame(lapply(preprocessed_dataset_adjusted[, expr_new_names], function(x) {
  if (all(is.na(x))) return(x)  # Evitar errores con columnas completamente vacías
  return(rescale(x, to = c(0, 1), na.rm = TRUE))
}))

# *** NUEVO CÓDIGO PARA FILTRAR FILAS ÚNICAS IGNORANDO "GEO_ID" ***
preprocessed_dataset_adjusted_not_GEO <- distinct(preprocessed_dataset_adjusted, across(-GEO_ID))

# *** NUEVO CÓDIGO PARA FILTRAR FILAS SIN NA EN GO_TERM ***
preprocessed_dataset_adjusted_not_GEO <- preprocessed_dataset_adjusted_not_GEO %>%
  filter(!is.na(GO_Term))

print(colnames(preprocessed_dataset_adjusted_not_GEO))

# *** NUEVO CÓDIGO PARA COMBINAR FILAS CASI DUPLICADAS ***
preprocessed_dataset_adjusted_not_GEO <- preprocessed_dataset_adjusted_not_GEO %>%
  group_by(Gene, C9, C10, across(starts_with("EX"))) %>% # Agrupar por las columnas que deben ser iguales
  summarise(
    GO_Term = paste(unique(GO_Term), collapse = "; "),
    X..GO_ID = paste(unique(X..GO_ID), collapse = "; "),
    C8 = paste(unique(C8), collapse = "; "),
    .groups = 'drop'
  )

# *** NUEVO CÓDIGO PARA REORDENAR Y RENOMBRAR COLUMNAS ***
preprocessed_dataset_adjusted_not_GEO <- preprocessed_dataset_adjusted_not_GEO %>%
  dplyr::select(Gene, X..GO_ID, GO_Term, C8, C9, C10, starts_with("EX"))

preprocessed_dataset_adjusted_not_GEO <- dplyr::rename(preprocessed_dataset_adjusted_not_GEO, GO_ID = X..GO_ID)

# 9. Guardar el dataset preprocesado
write.csv(preprocessed_dataset_adjusted_not_GEO, "preprocessed_dataset.csv", row.names = FALSE)

# *** NUEVO CÓDIGO PARA OBTENER Y GUARDAR LOS VALORES ÚNICOS DE GO_TERM ***

# 1. Separar los valores de GO_Term que están juntos por ";"
go_terms_separados <- preprocessed_dataset_adjusted_not_GEO %>%
  separate_longer_delim(GO_Term, delim = "; ")

# 2. Obtener los valores únicos de GO_Term
go_terms_unicos <- unique(go_terms_separados$GO_Term)

# 3. Crear un data frame con los valores únicos
df_go_terms_unicos <- data.frame(GO_Term = go_terms_unicos)

# 4. Guardar este data frame en un nuevo archivo .csv
write.csv(df_go_terms_unicos, "go_terms_unicos.csv", row.names = FALSE)

cat("Archivo 'go_terms_unicos.csv' guardado con los valores únicos de GO_Term.\n")
