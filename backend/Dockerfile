# 使用Eclipse Temurin JDK 17作为基础镜像
FROM eclipse-temurin:17-jdk-jammy

# 设置工作目录
WORKDIR /app

# 设置代理环境变量（如果需要的话，可以在docker build时通过--build-arg传入）
ARG HTTP_PROXY
ARG HTTPS_PROXY
ARG NO_PROXY=localhost,127.0.0.1

ENV http_proxy=${HTTP_PROXY}
ENV https_proxy=${HTTPS_PROXY}
ENV no_proxy=${NO_PROXY}

# 安装Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 创建Maven配置目录并添加阿里云镜像源
RUN mkdir -p /root/.m2
COPY settings.xml /root/.m2/settings.xml

# 复制pom.xml文件和源代码
COPY pom.xml .
COPY src ./src

# 构建应用（跳过dependency:go-offline，直接构建，添加重试机制）
RUN mvn clean package -DskipTests -Dmaven.wagon.http.retryHandler.count=5 || \
    (sleep 10 && mvn clean package -DskipTests -Dmaven.wagon.http.retryHandler.count=5) || \
    (sleep 20 && mvn clean package -DskipTests -Dmaven.wagon.http.retryHandler.count=5)

# 清除代理环境变量（运行时不需要）
ENV http_proxy=
ENV https_proxy=

# 暴露端口
EXPOSE 8080

# 启动应用
CMD ["java", "-jar", "target/certificate-system-1.0.0.jar"]