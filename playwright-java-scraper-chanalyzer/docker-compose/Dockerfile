FROM mcr.microsoft.com/playwright/java:v1.51.0-noble
WORKDIR /app
ARG JAVA_OPTS=""
ARG JAR_FILE=target/playwright-java-scraper-chanalyzer-1.0-SNAPSHOT.jar
COPY ${JAR_FILE} playwright-java-scraper-chanalyzer.jar
EXPOSE 5050
EXPOSE 5005
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar playwright-java-scraper-chanalyzer.jar"]