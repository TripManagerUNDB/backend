FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q
COPY src ./src
RUN ./mvnw clean package -DskipTests -q

FROM eclipse-temurin:25-jre-noble
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENV SPRING_DATA_MONGODB_URI="mongodb+srv://admin:admin@cluster0.x5bmhxf.mongodb.net/tripmanager?appName=Cluster0"
ENTRYPOINT ["java", "-jar", "app.jar"]
