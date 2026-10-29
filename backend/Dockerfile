# Etapa 1: Build con Maven y Eclipse Temurin JDK 21
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copiar archivos de Maven y dependencias
COPY pom.xml .
COPY src ./src

# Instalar Maven y empaquetar la aplicación omitiendo pruebas en el contenedor
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

# Etapa 2: Imagen final liviana con JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario sin privilegios por seguridad (OWASP)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiar el jar compilado desde la etapa builder
COPY --from=builder /app/target/comerciantes-backend-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
