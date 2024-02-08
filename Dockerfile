FROM openjdk:19-oracle

WORKDIR /app

COPY /target/obox.jar /app/obox.jar
COPY .env /app/

EXPOSE 8080

CMD ["java", "-jar", "obox.jar"]
