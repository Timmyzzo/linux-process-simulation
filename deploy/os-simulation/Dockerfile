FROM openjdk:21-jdk-slim
# 设定时区，防止日志时间不对
ENV TZ=Asia/Shanghai
WORKDIR /app
COPY app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]