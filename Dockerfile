# ---------- Etapa de build ----------
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Cacheo de dependencias
COPY pom.xml .
RUN mvn -q -e -B -DskipTests dependency:go-offline

# Copia del código y build
COPY src ./src
RUN mvn -q -e -B -DskipTests package

# ---------- Runtime ----------
FROM amazoncorretto:21-alpine
WORKDIR /app

# Copiamos el JAR compilado desde la etapa anterior
COPY --from=builder /workspace/target/*.jar /app/app.jar

# Puerto de la app (Render inyecta $PORT; Spring lo toma vía server.port)
EXPOSE 8080

# Opciones de JVM opcionales
ENV JAVA_OPTS=""

# Comando de arranque
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]