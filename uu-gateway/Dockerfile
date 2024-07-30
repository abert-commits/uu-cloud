FROM openjdk:11-jre

# 作者
LABEL org.opencontainers.image.authors="felix <felix@gmail.com>"

# 将可执行的jar包放到容器当中去
ADD ${JUU_FILE} app.jar

# 暴露8080端口
EXPOSE 20000

# JVM 调优参数
ENV JAVA_OPTION="-Dfile.encoding=UTF-8 -XX:+UseParallelGC -XX:+PrintGCDetails -Xloggc:/var/log/devops-example.gc.log -XX:+HeapDumpOnOutOfMemoryError -XX:+DisableExplicitGC"

# JVM 内存默认值
ENV XMX=2g
ENV XMS=2g
ENV XMN=1g
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
# 运行程序
#ENTRYPOINT ["sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar -Xmx${XMX} -Xms${XMS} -Xmn${XMN} $JAVA_OPTION -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} /app.jar"]
