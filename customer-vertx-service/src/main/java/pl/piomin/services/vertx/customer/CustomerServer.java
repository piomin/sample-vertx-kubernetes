package pl.piomin.services.vertx.customer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import pl.piomin.services.vertx.customer.client.AccountClient;
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
            repository.findById(rc.request().getParam("id"), res -> {
                Customer customer = res.result();
                LOGGER.info("Found by id: {}", customer);
                new AccountClient(vertx).findCustomerAccounts(customer.getId(), res2 -> {
                    customer.setAccounts(res2.result());
                    rc.response().end(customer.toString());
                });
            });
        });
        router.get("/customer/name/:name").produces("application/json").handler(rc -> {
            repository.findByName(rc.request().getParam("name"), res -> {
                List<Customer> customers = res.result();
                LOGGER.info("Found by name: {}", customers);
                rc.response().end(Json.encodePrettily(customers));
            });
        });
        router.get("/customer").produces("application/json").handler(rc -> {
            repository.findAll(res -> {
                List<Customer> customers = res.result();
                LOGGER.info("Found all: {}", customers);
                rc.response().end(Json.encodePrettily(customers));
            });
        });
        router.post("/customer").produces("application/json").handler(rc -> {
//            Customer c = Json.decodeValue(rc.body().asString(), Customer.class);
            Customer c = rc.body().asPojo(Customer.class);
            repository.save(c, res -> {
                Customer customer = res.result();
                LOGGER.info("Created: {}", customer);
                rc.response().end(customer.toString());
            });
        });
        router.delete("/customer/:id").handler(rc -> {
            repository.remove(rc.request().getParam("id"), res -> {
                LOGGER.info("Removed: {}", rc.request().getParam("id"));
                rc.response().setStatusCode(200);
            });
        });

        vertx.createHttpServer().requestHandler(router).listen(8080);

    }

}
