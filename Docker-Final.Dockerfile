FROM amazoncorretto:21-alpine

WORKDIR /app

ARG JAR_NAME
COPY ${JAR_NAME} /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]