library(Rtsne)

# Cargar datos desde CSV
datos <- read.csv("input_data.csv", header=TRUE, row.names=1)

# Ejecutar t-SNE
tsne_result <- Rtsne(datos, dims=2, perplexity=30, verbose=TRUE)

# Guardar resultados
write.csv(tsne_result$Y, file="output_tsne.csv")