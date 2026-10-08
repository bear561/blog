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
# cwebp：上传时生成 WebP 副本，供 nginx 按 Accept 头内容协商返回。
# 走阿里云源，官方 archive.ubuntu.com 从国内构建很慢（Alpine 那边已经踩过一次）。
# 下面两个源文件路径兼容 Ubuntu 22.04 的 sources.list 和 24.04 的 deb822 格式，缺失的忽略掉。
RUN sed -i -e 's|//archive.ubuntu.com|//mirrors.aliyun.com|g' \
           -e 's|//security.ubuntu.com|//mirrors.aliyun.com|g' \
           /etc/apt/sources.list /etc/apt/sources.list.d/ubuntu.sources 2>/dev/null || true; \
    apt-get update \
    && apt-get install -y --no-install-recommends webp \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/uploads
EXPOSE 8080
# 固定 JVM 时区为上海，避免 createdAt 等 LocalDateTime 与 JDBC serverTimezone=Asia/Shanghai 不一致导致 8 小时偏差
ENV TZ=Asia/Shanghai
ENTRYPOINT ["java", "-Duser.timezone=Asia/Shanghai", "-jar", "app.jar"]
