FROM amazoncorretto:23-alpine-jdk
ARG JAR_FILE=target/chanalyzer-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} chanalyzer.jar
ENTRYPOINT [ "java", "-jar", "/chanalyzer.jar" ]
EXPOSE 8080/tcp