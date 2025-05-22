# 1. Cargar BiocManager si no está instalado
if (!requireNamespace("BiocManager", quietly = TRUE))
  install.packages("BiocManager")

# 2. Cargar las librerías necesarias
BiocManager::install(c("GEOmetadb", "GEOquery", "dplyr", "tibble", "readr", "jsonlite", "httr", "DBI", "RSQLite", "stringr"))
library(GEOquery)
library(GEOmetadb)
library(dplyr)
library(tibble)
library(readr)
library(jsonlite)
library(httr)
library(DBI)
library(RSQLite)
library(stringr)

# 3. Función para obtener alias de un gen desde NCBI
get_gene_aliases <- function(gene_name) {
  base_url <- "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi"
  
  search_query <- list(db = "gene", term = paste0(gene_name, "[Gene Name] AND Homo sapiens[Organism]"), retmode = "json")
  search_response <- GET(base_url, query = search_query)
  
  search_data <- fromJSON(content(search_response, as = "text"))
  
  if (length(search_data$esearchresult$idlist) == 0) {
    print(paste("⚠️ No se encontró el gen:", gene_name))
    return(c(gene_name))  # Devolver solo el nombre original si no se encuentra
  }
  
  gene_id <- search_data$esearchresult$idlist[1]
  
  summary_url <- "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi"
  summary_query <- list(db = "gene", id = gene_id, retmode = "json")
  summary_response <- GET(summary_url, query = summary_query)
  
  summary_data <- fromJSON(content(summary_response, as = "text"))
  
  if (!is.null(summary_data$result[[gene_id]]$otheraliases)) {
    aliases <- unlist(strsplit(summary_data$result[[gene_id]]$otheraliases, ", "))
    return(unique(c(gene_name, aliases)))
  } else {
    return(c(gene_name))
  }
}

# 4. Descargar la base de datos GEO si no existe
if (!file.exists("GEOmetadb.sqlite")) {
  getSQLiteFile()
}

# 5. Conectar a la base de datos GEOmetadb
con <- dbConnect(SQLite(), "GEOmetadb_demo.sqlite")

# 6. Cargar el dataset original
original_dataset <- read.csv("../GF-Miner_miner-gene-function.tsv", sep="\t", header=TRUE)
original_dataset <- original_dataset %>%
  select(Gene, X..GO_ID, C8, C9, C10) %>%
  filter(!is.na(Gene) & Gene != "")

# 7. Obtener alias de los genes
gene_alias_map <- list()
all_genes <- unique(original_dataset$Gene)

for (gene in all_genes) {
  gene_alias_map[[gene]] <- get_gene_aliases(gene)
}

gene_alias_df <- do.call(rbind, lapply(names(gene_alias_map), function(gene) {
  data.frame(Gene = gene, Alias = paste(gene_alias_map[[gene]], collapse = ";"), stringsAsFactors = FALSE)
}))

write.csv(gene_alias_df, "gene_alias_map.csv", row.names = FALSE)

remaining_genes <- all_genes
found_genes <- c()
all_results <- list()
used_gse_ids <- c()
i <- 1

# 8. Buscar datasets en humanos
query <- "SELECT gds.gse, gds.title, gds.sample_count FROM gds 
          JOIN gse ON gds.gse = gse.gse 
          WHERE gds.sample_organism LIKE '%Homo sapiens%' 
          AND gds.type LIKE '%Expression profiling by array%' 
          ORDER BY gds.sample_count DESC;"

dataset_info <- dbGetQuery(con, query)

# Verificar si hay datasets disponibles
if (nrow(dataset_info) == 0) {
  print("❌ No hay datasets disponibles en GEO.")
  dbDisconnect(con)
  stop("Finalizando ejecución.")
}

while (length(remaining_genes) > 0 && i <= nrow(dataset_info)) {
  gse_id <- dataset_info$gse[i]
  print(paste("🔍 Evaluando dataset:", gse_id))
  
  tryCatch({
    gset <- getGEO(gse_id, GSEMatrix = TRUE, AnnotGPL = TRUE)
    
    if (length(gset) == 0) {
      print(paste("⚠️ Error: No se pudo descargar", gse_id))
      i <- i+1
      next
    }
    
    exprs_data <- exprs(gset[[1]])
    annotation_data <- fData(gset[[1]])
    
    possible_gene_cols <- c("Gene symbol", "gene_assignment", "Gene", "Symbol")
    gene_col <- intersect(possible_gene_cols, colnames(annotation_data))
    
    if (length(gene_col) == 0) {
      print(paste("⚠️ Error: No se encontró la columna de genes en", gse_id))
      i <- i+1
      next
    } else {
      gene_col <- gene_col[1]
    }
    
    annotation_data <- annotation_data %>%
      rownames_to_column(var = "GEO_ID") %>%
      select(GEO_ID, Gene = all_of(gene_col)) %>%
      filter(!is.na(Gene) & Gene != "")
    
    # 9. Buscar genes en GEO incluyendo alias y excluir los ya encontrados
    new_genes <- setdiff(
      unique(unlist(lapply(remaining_genes, function(g) {
        aliases <- gene_alias_map[[g]]
        annotation_data$Gene[annotation_data$Gene %in% aliases]
      }))),
      found_genes
    )
    
    if (length(new_genes) == 0) {
      print(paste("🔹 El dataset", gse_id, "no aporta nuevos genes. Omitiendo."))
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
      print(paste("⚠️ No se encontraron datos de expresión en", gse_id))
      i <- i+1
      next
    }
    
    filtered_exprs <- as.data.frame(filtered_exprs) %>%
      rownames_to_column(var = "GEO_ID") %>%
      left_join(annotation_data, by = "GEO_ID") %>%
      group_by(Gene) %>%
      summarise(across(where(is.numeric), mean, na.rm = TRUE))
    
    merged_dataset <- left_join(gene_filtered, filtered_exprs, by = "Gene")
    
    all_results[[gse_id]] <- merged_dataset
    used_gse_ids <- unique(c(used_gse_ids, gse_id))
    
    # 11. Eliminar genes encontrados y sus alias de la lista
    found_genes <- unique(c(found_genes, new_genes))
    remaining_genes <- setdiff(remaining_genes, found_genes)
    
    print(paste("✅ Genes encontrados:", length(found_genes)))
    print(paste("❌ Genes restantes:", length(remaining_genes)))
    
  }, error = function(e) {
    print(paste("⚠️ Error en", gse_id, ":", e$message))
  })
  
  i <- i+1
}

if (length(remaining_genes) > 0) {
  print("❌ No se encontraron todos los genes. Genes restantes:")
  print(remaining_genes)
  write.csv(remaining_genes, "remaning_genes.csv", row.names = FALSE)
} else {
  print("🎉 Todos los genes han sido encontrados.")
}

if (length(all_results) > 0) {
  final_dataset <- bind_rows(all_results)
  write.csv(final_dataset, "merged_dataset.csv", row.names = FALSE)
  print("Proceso completado. Datos guardados en merged_dataset.csv")
} else {
  print("No se generaron datos. Verifica los errores en los datasets.")
}

dbDisconnect(con)
