FROM vertx/vertx3
ENV VERTICLE_NAME pl.piomin.services.vertx.customer.CustomerServer
ENV VERTICLE_FILE target/customer-vertx-service-1.0-SNAPSHOT.jar
ENV VERTICLE_HOME /usr/verticles
EXPOSE 8090
COPY $VERTICLE_FILE $VERTICLE_HOME/
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec vertx run $VERTICLE_NAME -cp $VERTICLE_HOME/*"]