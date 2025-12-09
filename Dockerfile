
# ---------- BUILD STAGE ----------
FROM maven:3.8.5-eclipse-temurin-17 AS build
 
WORKDIR /app
 
# Copy only pom.xml first (for caching)
COPY sales/pom.xml .
 
RUN mvn dependency:go-offline -B
 
# Copy full backend source
COPY sales/. .
 
# Build jar
RUN mvn package -DskipTests -B
 
 
# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:17-jre
 
WORKDIR /app
 
COPY --from=build /app/target/*.jar app.jar
 
EXPOSE 8080
 
ENTRYPOINT ["java", "-jar", "app.jar"]
