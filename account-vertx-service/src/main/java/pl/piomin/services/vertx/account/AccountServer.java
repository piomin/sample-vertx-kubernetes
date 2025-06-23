package pl.piomin.services.vertx.account;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.piomin.services.vertx.account.data.Account;
import pl.piomin.services.vertx.account.data.AccountRepository;


public class AccountServer extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServer.class);

    public static void main(String[] args) throws Exception {
        System.setProperty("vertx.disableFileCPResolving", "true");
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MongoVerticle());
        vertx.deployVerticle(new AccountServer());
    }

    @Override
    public void start() throws Exception {
        AccountRepository repository = AccountRepository.createProxy(vertx, "account-service");

        Router router = Router.router(vertx);
        router.route("/account/*").handler(ResponseContentTypeHandler.create());
        router.route(HttpMethod.POST, "/account").handler(BodyHandler.create());

        router.get("/account/:id").produces("application/json").handler(rc -> {
            repository.findById(rc.request().getParam("id")).onComplete(res -> {
                if (res.succeeded()) {
                    Account a = res.result();
                    LOGGER.info("Found by id: {}", a);
                    if (a == null) {
                        rc.response().setStatusCode(404).end();
                    } else {
                        rc.response().end(Json.encodePrettily(a));
                    }
                } else {
                    rc.response().setStatusCode(500).end();
                }
            });
        });

        router.get("/account/customer/:customer").produces("application/json").handler(rc -> {
            repository.findByCustomer(rc.request().getParam("customer")).onComplete(res -> {
                if (res.succeeded()) {
                    LOGGER.info("Found by customer: {}", res.result());
                    rc.response().end(Json.encodePrettily(res.result()));
                } else {
                    rc.response().setStatusCode(500).end();
                }
            });
        });

        router.get("/account").produces("application/json").handler(rc -> {
            repository.findAll().onComplete(res -> {
                if (res.succeeded()) {
                    LOGGER.info("Found all: {}", res.result());
                    rc.response().end(Json.encodePrettily(res.result()));
                } else {
                    rc.response().setStatusCode(500).end();
                }
            });
        });

        router.post("/account").produces("application/json").handler(rc -> {
            Account a = rc.body().asPojo(Account.class);
            repository.save(a).onComplete(res -> {
                if (res.succeeded()) {
                    rc.response().end(Json.encodePrettily(res.result()));
                    LOGGER.info("Created: {}", res.result());
                } else {
                    rc.response().setStatusCode(500).end();
                }
            });
        });

        router.delete("/account/:id").handler(rc -> {
            repository.remove(rc.request().getParam("id"));
            rc.response().setStatusCode(200);
        });

        vertx.createHttpServer().requestHandler(router).listen(8080);

    }

}
