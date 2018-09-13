package pl.piomin.services.vertx.customer.client;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.vertx.ext.web.client.WebClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.client.WebClient;
import pl.piomin.services.vertx.customer.data.Account;

public class AccountClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccountClient.class);
	
	private Vertx vertx;

	public AccountClient(Vertx vertx) {
		this.vertx = vertx;
	}
	
	public AccountClient findCustomerAccounts(String customerId, Handler<AsyncResult<List<Account>>> resultHandler) {
		WebClient client = WebClient.create(vertx);
		client.get(8095, "account-service", "/account/customer/" + customerId).timeout(1000).send(res2 -> {
			if (res2.succeeded()) {
				LOGGER.info("Response: {}", res2.result().bodyAsString());
				List<Account> accounts = res2.result().bodyAsJsonArray().stream().map(it -> Json.decodeValue(it.toString(), Account.class)).collect(Collectors.toList());
				resultHandler.handle(Future.succeededFuture(accounts));
			} else {
				resultHandler.handle(Future.succeededFuture(new ArrayList<>()));
			}
		});
		return this;
	}
	
}
