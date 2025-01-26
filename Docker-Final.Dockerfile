FROM amazoncorretto:21-alpine

WORKDIR /app

ARG JAR_NAME
COPY ${JAR_NAME} /app/app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]