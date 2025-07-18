package pl.piomin.services.vertx.customer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.piomin.services.vertx.customer.data.Customer;
import pl.piomin.services.vertx.customer.data.CustomerRepository;


public class CustomerServer extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerServer.class);

    public static void main(String[] args) throws Exception {
        System.setProperty("vertx.disableFileCPResolving", "true");
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MongoVerticle());
        vertx.deployVerticle(new CustomerServer());
    }

    @Override
    public void start() throws Exception {
        CustomerRepository repository = CustomerRepository.createProxy(vertx, "customer-service");

        Router router = Router.router(vertx);
        router.route("/customer/*").handler(ResponseContentTypeHandler.create());
        router.route(HttpMethod.POST, "/customer").handler(BodyHandler.create());


        router.get("/customer/:id").produces("application/json").handler(rc -> {
            repository.findById(rc.request().getParam("id")).onComplete(res -> {
                if (res.succeeded()) {
                    Customer c = res.result();
                    LOGGER.info("Found by id: {}", c);
                    if (c == null) {
                        rc.response().setStatusCode(404).end();
                    } else {
                        rc.response().end(Json.encodePrettily(c));
                    }
                } else {
                    rc.response().setStatusCode(500).end();
                }
            });
        });

        router.get("/customer/name/:name").produces("application/json").handler(rc -> {
            repository.findByName(rc.request().getParam("name")).onComplete(res -> {
                if (res.succeeded()) {
                    LOGGER.info("Found by name: {}", res.result());
                    rc.response().end(Json.encodePrettily(res.result()));
                } else {
                    rc.response().setStatusCode(500).end();
                }
            });
        });

        router.get("/customer").produces("application/json").handler(rc -> {
            repository.findAll().onComplete(res -> {
                if (res.succeeded()) {
                    LOGGER.info("Found all: {}", res.result());
                    rc.response().end(Json.encodePrettily(res.result()));
                } else {
                    rc.response().setStatusCode(500).end();
                }
            });
        });

        router.post("/customer").produces("application/json").handler(rc -> {
            Customer c = rc.body().asPojo(Customer.class);
            repository.save(c).onComplete(res -> {
                if (res.succeeded()) {
                    LOGGER.info("Created: {}", c);
                    rc.response().end(Json.encodePrettily(res.result()));
                } else {
                    rc.response().setStatusCode(500).end();
                }
            });
        });

        router.delete("/customer/:id").handler(rc -> {
            repository.remove(rc.request().getParam("id"));
            rc.response().setStatusCode(200);
        });

        vertx.createHttpServer().requestHandler(router).listen(8080);

    }

}
