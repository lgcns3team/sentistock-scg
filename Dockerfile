FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Gradle bootJar 결과물
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8088

ENTRYPOINT ["java","-jar","/app/app.jar"]
