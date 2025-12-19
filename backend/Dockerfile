# 使用Eclipse Temurin JDK 17作为基础镜像
FROM eclipse-temurin:17-jdk-jammy

# 设置工作目录
WORKDIR /app

# 安装Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 创建Maven配置目录并添加阿里云镜像源
RUN mkdir -p /root/.m2
COPY settings.xml /root/.m2/settings.xml

# 复制pom.xml文件
COPY pom.xml .

# 下载依赖（利用Docker缓存层）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 暴露端口
EXPOSE 8080

# 启动应用
CMD ["java", "-jar", "target/certificate-system-1.0.0.jar"]