## Build stage with full JDK
FROM eclipse-temurin:23-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -q -DskipTests

# Runtime stage with only JRE
FROM eclipse-temurin:23-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/playwright-java-scraper-chanalyzer-1.0-SNAPSHOT.jar playwright-java-scraper-chanalyzer.jar
EXPOSE 5050
ENTRYPOINT ["java", "-jar", "playwright-java-scraper-chanalyzer.jar"]