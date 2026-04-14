FROM gradle:8.7-jdk21 AS builder

WORKDIR /app
COPY . .

RUN chmod +x gradlew
RUN ./gradlew clean shadowJar -x test

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*-all.jar app.jar

EXPOSE 7000
EXPOSE 50051

CMD ["java", "-jar", "app.jar"]