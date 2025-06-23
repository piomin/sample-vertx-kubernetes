package pl.piomin.services.vertx.customer.client;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.piomin.services.vertx.customer.data.Account;

import java.util.List;
import java.util.stream.Collectors;

public class AccountClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountClient.class);

    private Vertx vertx;

    public AccountClient(Vertx vertx) {
        this.vertx = vertx;
    }

    public AccountClient findCustomerAccounts(String customerId, Handler<AsyncResult<List<Account>>> resultHandler) {
        WebClient client = WebClient.create(vertx);
        client.get(8080, "account-vertx-service", "/account/customer/" + customerId)
                .send()
                .onComplete(res2 -> {
                    LOGGER.info("Response: {}", res2.result().bodyAsString());
                    List<Account> accounts = res2.result()
                            .bodyAsJsonArray()
                            .stream()
                            .map(it -> Json.decodeValue(it.toString(), Account.class))
                            .toList();
                    resultHandler.handle(Future.succeededFuture(accounts));
                });
        return this;
    }

}
