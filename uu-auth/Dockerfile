# 该镜像需要依赖的java8的镜像
FROM openjdk:8-jre
 

VOLUME ["/tmp"]
 

EXPOSE 20001

ADD ${JUU_FILE} app.jar
 

 

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
 

MAINTAINER felix/felix@gmail.com