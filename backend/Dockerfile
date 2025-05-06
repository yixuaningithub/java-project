FROM eclipse-temurin:21

WORKDIR /app

COPY . .

RUN ./mvnw package -DskipTests

CMD ["java", "-jar", "target/portfolio-project-0.0.1-SNAPSHOT.jar"]
