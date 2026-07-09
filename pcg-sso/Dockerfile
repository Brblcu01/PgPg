FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd -r -u 10001 appuser
USER appuser

COPY target/*.jar /app/app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
