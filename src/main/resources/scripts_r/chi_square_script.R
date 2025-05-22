library(MASS)

# Cargar datos desde CSV
datos <- read.csv("input_data.csv", header=TRUE, row.names=1)
etiquetas <- read.csv("labels.csv", header=FALSE)$V1

# Aplicar Chi-cuadrado
resultados <- apply(datos, 2, function(x) chisq.test(x, etiquetas)$p.value)

# Filtrar características con p-valor < 0.05
seleccionadas <- names(resultados[resultados < 0.05])

# Guardar resultados
write.csv(seleccionadas, file="output_chi_square.csv")