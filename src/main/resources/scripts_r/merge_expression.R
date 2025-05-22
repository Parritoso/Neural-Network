# if (!require("BiocManager", quietly = TRUE))
#   install.packages("BiocManager")
# 
# BiocManager::install("GEOmetadb")
# BiocManager::install("GEOquery")
# library(GEOquery)
# library(GEOmetadb)
# library(dplyr)
# library(readr)
# library(tibble)  # Asegurar que tibble esté cargado
# 
# # Descargar la base de datos GEO si no existe
# if( !file.exists("GEOmetadb.sqlite") ) {
#   demo_sqlfile <- getSQLiteFile(destdir = getwd(), destfile = "GEOmetadb.sqlite.gz")
# } else {
#   demo_sqlfile <- "GEOmetadb.sqlite"
# }
# 
# # Conectar a la base de datos GEOmetadb
# con <- dbConnect(SQLite(), "GEOmetadb_demo.sqlite")
# 
# geo_tables <- dbListTables(con)
# geo_tables
# 
# # Buscar datasets de expresión génica en humanos
# query <- "SELECT gds.gse, gds.title, gds.sample_count FROM gds 
#           JOIN gse ON gds.gse = gse.gse 
#           WHERE gds.sample_organism LIKE '%Homo sapiens%' 
#           AND gds.type LIKE '%Expression profiling by array%' 
#           ORDER BY gds.sample_count DESC LIMIT 1;"
# dataset_info <- dbGetQuery(con, query)
# 
# # Seleccionar el dataset con más muestras
# gse_id <- dataset_info$gse[1]
# 
# # Descargar el dataset seleccionado
# gset <- getGEO(gse_id, GSEMatrix = TRUE, AnnotGPL = TRUE)
# exprs_data <- exprs(gset[[1]])  # Extraer la matriz de expresión
# 
# # Obtener anotaciones de genes
# annotation_data <- fData(gset[[1]])
# 
# # Identificar la columna de nombres de genes en la anotación
# if ("Gene symbol" %in% colnames(annotation_data)) {
#   gene_col <- "Gene symbol"
# } else if ("gene_assignment" %in% colnames(annotation_data)) {
#   gene_col <- "gene_assignment"
# } else {
#   stop("No se encontró una columna de símbolos de genes en la anotación.")
# }
# 
# # Convertir nombres de fila en una columna
# annotation_data <- annotation_data %>%
#   rownames_to_column(var = "GEO_ID")  
# 
# # Crear un mapeo entre IDs de GEO y nombres de genes
# gene_map <- annotation_data %>%
#   select(GEO_ID, Gene = all_of(gene_col)) %>%
#   filter(!is.na(Gene) & Gene != "")
# 
# # Cargar el dataset original con los genes de interés
# original_dataset <- read.csv("../GF-Miner_miner-gene-function.tsv", sep="\t", header=TRUE)
# 
# gene_name_dataset <- original_dataset[!duplicated(original_dataset$Gene),]
# 
# # Mapear los nombres de genes en el dataset original a los IDs de GEO
# gene_name_dataset <- gene_name_dataset %>%
#   left_join(gene_map, by = "Gene")
# 
# # Filtrar solo los genes que tienen correspondencia
# gene_filtered <- gene_name_dataset %>%
#   filter(!is.na(GEO_ID))
# 
# # Filtrar la matriz de expresión para los genes presentes en el dataset original
# filtered_exprs <- exprs_data[rownames(exprs_data) %in% gene_filtered$GEO_ID, ]
# 
# # Filtrar solo los genes que aparecen en el dataset original
# #genes_of_interest <- unique(gene_name_dataset$Gene)
# #filtered_exprs <- exprs_data[rownames(exprs_data) %in% genes_of_interest, ]
# 
# # Promediar la expresión de todas las sondas asociadas a un mismo gen
# exprs_avg <- data.frame(GEO_ID = rownames(filtered_exprs), Expression = rowMeans(filtered_exprs, na.rm = TRUE)) %>%
#   left_join(gene_map, by = "GEO_ID") %>%
#   group_by(Gene) %>%
#   summarise(Expression = mean(Expression, na.rm = TRUE))
# 
# # Fusionar con el dataset original considerando genes repetidos
# merged_dataset <- left_join(gene_filtered, exprs_avg, by = "Gene")
# 
# # Guardar el dataset final
# write.csv(merged_dataset, "merged_dataset.csv", row.names=FALSE)
# 
# # Cerrar la conexión a la base de datos
# dbDisconnect(con)
# 
# print(paste("Proceso completado. Se utilizó el dataset:", gse_id, "Datos guardados en merged_dataset.csv"))
# 1. Cargar BiocManager si no está instalado
if (!requireNamespace("BiocManager", quietly = TRUE))
  install.packages("BiocManager")

# 2. Cargar las librerías necesarias
BiocManager::install(c("GEOmetadb", "GEOquery", "dplyr", "tibble", "readr"))
library(GEOquery)
library(GEOmetadb)
library(dplyr)
library(tibble)
library(readr)
library(jsonlite)
library(httr)
library(DBI)
library(RSQLite)

# 3. Descargar la base de datos GEO si no existe
if (!file.exists("GEOmetadb.sqlite")) {
  getSQLiteFile()
}

# 4. Conectar a la base de datos GEOmetadb
con <- dbConnect(SQLite(), "GEOmetadb_demo.sqlite")

# 5-15. Bucle para iterar hasta cubrir todos los genes del dataset original
original_dataset <- read.csv("../GF-Miner_miner-gene-function.tsv", sep="\t", header=TRUE)
original_dataset <- original_dataset %>%
  select(Gene, GO_ID, C8, C9, C10) %>%
  filter(!is.na(Gene) & Gene != "")
remaining_genes <- unique(original_dataset$Gene)
found_genes <- c()  # Lista de genes ya encontrados
all_results <- list()
used_gse_ids <- c()  # Vector para almacenar los GSE_IDs utilizados
i <- 1

while (length(remaining_genes) > 0 || gse_id == c("NA")) {
  # 5. Buscar datasets de expresión génica en humanos con la query
  query <- "SELECT gds.gse, gds.title, gds.sample_count FROM gds 
            JOIN gse ON gds.gse = gse.gse 
            WHERE gds.sample_organism LIKE '%Homo sapiens%' 
            AND gds.type LIKE '%Expression profiling by array%' 
            ORDER BY gds.sample_count DESC;"
  
  dataset_info <- dbGetQuery(con, query)
  
  if (nrow(dataset_info) == 0) {
    print("No se encontraron más datasets disponibles.")
    break
  }
  
  # 6. Seleccionar el dataset con más muestras
  gse_id <- dataset_info$gse[i]
  print(paste("Descargando dataset:", gse_id))
  
  # 8. Descargar el dataset seleccionado con manejo de errores
  tryCatch({
    gset <- getGEO(gse_id, GSEMatrix = TRUE, AnnotGPL = TRUE)
    
    if (length(gset) == 0) {
      print(paste("Error: No se pudo descargar", gse_id, "- Se intentará con otro."))
      next
    }
    
    exprs_data <- exprs(gset[[1]])  # Matriz de expresión
    annotation_data <- fData(gset[[1]])  # Anotaciones de genes
    
    # 9. Identificar la columna de nombres de genes en la anotación
    if ("Gene symbol" %in% colnames(annotation_data)) {
      gene_col <- "Gene symbol"
    } else if ("gene_assignment" %in% colnames(annotation_data)) {
      gene_col <- "gene_assignment"
    } else {
      print(paste("Error: No se encontró la columna de genes en", gse_id))
      i <- i+1
      next
    }
    
    # 10. Convertir nombres de fila en una columna y mapear IDs GEO a nombres de genes
    annotation_data <- annotation_data %>%
      rownames_to_column(var = "GEO_ID") %>%
      select(GEO_ID, Gene = all_of(gene_col)) %>%
      filter(!is.na(Gene) & Gene != "")
    
    # Filtrar solo genes NO encontrados previamente
    new_genes <- setdiff(annotation_data$Gene, found_genes)
    
    if (length(new_genes) == 0) {
      print(paste("El dataset", gse_id, "no aporta genes nuevos. Se omitirá."))
      i <- i+1
      next
    }
    
    # 11. Duplicar el dataset original para no modificarlo
    working_dataset <- original_dataset
    
    # 12. Mapear los nombres de genes en el dataset original a los IDs de GEO
    working_dataset <- working_dataset %>%
      left_join(annotation_data, by = "Gene")
    
    # 13. Filtrar solo los genes que tienen correspondencia
    gene_filtered <- working_dataset %>%
      filter(Gene %in% new_genes & !is.na(GEO_ID))
    
    # 14. Filtrar la matriz de expresión para los genes presentes en el dataset original
    filtered_exprs <- exprs_data[rownames(exprs_data) %in% gene_filtered$GEO_ID, ]
    
    if (nrow(filtered_exprs) == 0) {
      print(paste("Error: No se encontraron datos de expresión para", gse_id))
      i <- i+1
      next
    }
    
    # 15. Promediar la expresión de todas las sondas asociadas a un mismo gen por muestra
    filtered_exprs <- as.data.frame(filtered_exprs) %>%
      rownames_to_column(var = "GEO_ID") %>%
      left_join(annotation_data, by = "GEO_ID") %>%
      group_by(Gene) %>%
      summarise(across(where(is.numeric), mean, na.rm = TRUE))
    
    # 16. Fusionar con el dataset original considerando genes repetidos
    merged_dataset <- left_join(gene_filtered, filtered_exprs, by = "Gene")
    
    # 17. Actualizar lista de genes restantes
    found_genes <- unique(c(found_genes, new_genes))
    remaining_genes <- setdiff(remaining_genes, found_genes)
    
    # Guardar los resultados parciales
    all_results[[gse_id]] <- merged_dataset
    
    # 18. Guardar el GSE_ID que ha servido para completar el dataset
    used_gse_ids <- unique(c(used_gse_ids, gse_id))
    
  }, error = function(e) {
    print(paste("Error en la descarga del dataset", gse_id, ":", e$message))
  })
  i <- i+1
}

# 18. Guardar el dataset final
if (length(all_results) > 0) {
  final_dataset <- bind_rows(all_results)
  write.csv(final_dataset, "merged_dataset.csv", row.names = FALSE)
  print("Proceso completado. Datos guardados en merged_dataset.csv")
} else {
  print("No se generaron datos. Verifica los errores en los datasets.")
}

# 20. Guardar la lista de GSE_IDs utilizados
if (length(used_gse_ids) > 0) {
  writeLines(used_gse_ids, "used_gse_ids.txt")
  print("Lista de GSE_IDs utilizados guardada en used_gse_ids.txt")
  print("GSE_IDs utilizados:")
  print(used_gse_ids)
} else {
  print("No se encontraron datasets válidos para la extracción de datos.")
}

# # 2. Cargar el dataset final y la lista de GSE_IDs utilizados
# if (file.exists("merged_dataset.csv")) {
#   merged_dataset <- read.csv("merged_dataset.csv", header = TRUE)
#   found_genes <- unique(merged_dataset$Gene)
# } else {
#   stop("Error: No se encontró el archivo merged_dataset.csv. Asegúrate de ejecutar el proceso principal primero.")
# }
# 
# if (file.exists("used_gse_ids.txt")) {
#   used_gse_ids <- readLines("used_gse_ids.txt")
# } else {
#   used_gse_ids <- c()
# }
# 
# # 3. Determinar los genes faltantes
# missing_genes <- setdiff(original_dataset$Gene, found_genes)
# if (length(missing_genes) == 0) {
#   print("No hay genes faltantes. El dataset ya está completo.")
# } else {
#   print(paste("Se encontraron", length(missing_genes), "genes faltantes. Buscando datasets adicionales..."))
# }
# 
# # 4. Función para obtener alias de un gen desde NCBI/GEO
# get_gene_aliases <- function(gene_name) {
#   base_url <- "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi"
# 
#   # Paso 1: Buscar el ID del gen en NCBI
#   search_query <- list(db = "gene", term = paste0(gene_name, "[Gene Name] AND Homo sapiens[Organism]"), retmode = "json")
#   search_response <- GET(base_url, query = search_query)
# 
#   search_data <- fromJSON(content(search_response, as = "text"))
# 
#   if (length(search_data$esearchresult$idlist) == 0) {
#     print(paste("⚠️ No se encontró el gen:", gene_name))
#     return(c(gene_name))  # Devolver solo el nombre original si no se encuentra
#   }
# 
#   gene_id <- search_data$esearchresult$idlist[1]
# 
#   # Paso 2: Obtener detalles del gen (incluyendo alias)
#   summary_url <- "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi"
#   summary_query <- list(db = "gene", id = gene_id, retmode = "json")
#   summary_response <- GET(summary_url, query = summary_query)
# 
#   summary_data <- fromJSON(content(summary_response, as = "text"))
# 
#   if (!is.null(summary_data$result[[gene_id]]$otheraliases)) {
#     aliases <- unlist(strsplit(summary_data$result[[gene_id]]$otheraliases, ", "))
#     return(unique(c(gene_name, aliases)))  # Devolver lista de nombres
#   } else {
#     return(c(gene_name))  # Si no tiene alias, devolver solo el original
#   }
# }
# 
# # 4. Procesar cada gen faltante individualmente
# for (gene in missing_genes) {
#   aliases <- get_gene_aliases(gene)
#   print(paste("Buscando dataset para el gen:", gene, "y sus alias:", paste(aliases, collapse = ", ")))
#   
#   found <- FALSE
#   
#   for (gene_name in aliases) {
#     query <- paste0("SELECT DISTINCT gds.gse 
#                      FROM gds 
#                      JOIN gse ON gds.gse = gse.gse
#                      JOIN gpl ON gds.gpl = gpl.gpl 
#                      WHERE gds.sample_organism LIKE '%Homo sapiens%' 
#                      AND gds.type LIKE '%Expression profiling by array%' 
#                      AND (gpl.title LIKE '%", gene_name, "%' OR gpl.manufacturer LIKE '%", gene_name, "%' OR gpl.description LIKE '%", gene_name ,"%')
#                      AND gds.gse NOT IN ('", paste(used_gse_ids, collapse = "','"), "')
#                      ORDER BY RANDOM()
#                      LIMIT 1;")
#     
#     dataset_info <- dbGetQuery(con, query)
#     
#     if (nrow(dataset_info) == 0) {
#       print(paste("No se encontró un dataset para el gen:", gene_name))
#       next
#     }
#     
#     gse_id <- dataset_info$gse[1]
#     print(paste("Descargando dataset:", gse_id, "para el gen", gene_name))
#     
#     tryCatch({
#       gset <- getGEO(gse_id, GSEMatrix = TRUE, AnnotGPL = TRUE)
#       
#       if (length(gset) == 0) {
#         print(paste("Error: No se pudo descargar", gse_id, "- Se intentará con otro."))
#         next
#       }
#       
#       exprs_data <- exprs(gset[[1]])
#       annotation_data <- fData(gset[[1]])
#       
#       if ("Gene symbol" %in% colnames(annotation_data)) {
#         gene_col <- "Gene symbol"
#       } else if ("gene_assignment" %in% colnames(annotation_data)) {
#         gene_col <- "gene_assignment"
#       } else {
#         print(paste("Error: No se encontró la columna de genes en", gse_id))
#         next
#       }
#       
#       annotation_data <- annotation_data %>%
#         rownames_to_column(var = "GEO_ID") %>%
#         select(GEO_ID, Gene = all_of(gene_col)) %>%
#         filter(!is.na(Gene) & Gene != "")
#       
#       if (!(gene_name %in% annotation_data$Gene)) {
#         print(paste("El dataset", gse_id, "no contiene el gen", gene_name))
#         next
#       }
#       
#       # Filtrar la matriz de expresión para el gen buscado
#       filtered_exprs <- exprs_data[rownames(exprs_data) %in% annotation_data$GEO_ID, ]
#       
#       if (nrow(filtered_exprs) == 0) {
#         print(paste("Error: No se encontraron datos de expresión para el gen", gene_name))
#         next
#       }
#       
#       filtered_exprs <- as.data.frame(filtered_exprs) %>%
#         rownames_to_column(var = "GEO_ID") %>%
#         left_join(annotation_data, by = "GEO_ID") %>%
#         group_by(Gene) %>%
#         summarise(across(where(is.numeric), mean, na.rm = TRUE))
#       
#       merged_dataset <- bind_rows(merged_dataset, filtered_exprs)
#       found_genes <- unique(c(found_genes, gene))
#       used_gse_ids <- unique(c(used_gse_ids, gse_id))
#       found <- TRUE
#       
#       print(paste("El gen", gene_name, "se añadió correctamente al dataset."))
#       break  # Salir del loop si se encontró el gen en un dataset
#       
#     }, error = function(e) {
#       print(paste("Error en la descarga del dataset", gse_id, ":", e$message))
#     })
#   }
#   
#   if (!found) {
#     print(paste("No se encontró un dataset válido para el gen:", gene))
#   }
# }
# 
# # 5. Guardar el dataset final actualizado
# write.csv(merged_dataset, "merged_dataset.csv", row.names = FALSE)
# writeLines(used_gse_ids, "used_gse_ids.txt")

# 17. Cerrar la conexión a la base de datos
dbDisconnect(con)

print("Proceso completado. Datos guardados en merged_dataset.csv")
print("Lista final de GSE_IDs utilizados guardada en used_gse_ids.txt")
