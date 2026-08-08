FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY settings.xml /root/.m2/settings.xml
COPY pom.xml .
# 先下载依赖（利用 Docker 缓存层）
RUN mvn dependency:go-offline -B -DskipTests -s /root/.m2/settings.xml
COPY src ./src
RUN mvn package -DskipTests -B -s /root/.m2/settings.xml

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/uploads
EXPOSE 8080
# 固定 JVM 时区为上海，避免 createdAt 等 LocalDateTime 与 JDBC serverTimezone=Asia/Shanghai 不一致导致 8 小时偏差
ENV TZ=Asia/Shanghai
ENTRYPOINT ["java", "-Duser.timezone=Asia/Shanghai", "-jar", "app.jar"]
