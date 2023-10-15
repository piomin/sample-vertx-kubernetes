FROM eclipse-temurin:21-alpine
ENV VERTICLE_FILE account-vertx-service-1.0-SNAPSHOT.jar
ENV VERTICLE_HOME /usr/verticles
EXPOSE 8080
COPY target/$VERTICLE_FILE $VERTICLE_HOME/
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["java -jar $VERTICLE_FILE"]