FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests


CMD ["java","-jar","target/Health-Check_Utility-0.0.1-SNAPSHOT.jar"]
