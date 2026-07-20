# Step 1: Build JAR on host machine first
#   cd blog-server && mvn package -DskipTests
#
# Step 2: Build Docker image
#   docker build -t blog-server .

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/*.jar app.jar
RUN mkdir -p /app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
