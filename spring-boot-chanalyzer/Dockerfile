FROM amazoncorretto:23-alpine-jdk
ARG JAR_FILE=target/chanalyzer-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} chanalyzer.jar
COPY application-prod.properties application-prod.properties
CMD [ "java", "-jar", "/chanalyzer.jar", "--spring.profiles.active=prod" ]
EXPOSE 8080/tcp