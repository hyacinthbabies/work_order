FROM openjdk:8-jdk-alpine

WORKDIR /app

# 安装Maven
RUN apk add --no-cache maven

# 复制pom.xml并下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn package -DskipTests -B

# 暴露端口
EXPOSE 8080

# 运行应用
CMD ["java", "-jar", "target/ticket-system-1.0.0.jar"]