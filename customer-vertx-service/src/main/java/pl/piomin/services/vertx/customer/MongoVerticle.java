package pl.piomin.services.vertx.customer;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.piomin.services.vertx.customer.data.CustomerRepository;
import pl.piomin.services.vertx.customer.data.CustomerRepositoryImpl;

public class MongoVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(MongoVerticle.class);

	@Override
	public void start() throws Exception {
		ConfigStoreOptions envStore = new ConfigStoreOptions()
				.setType("env")
				.setConfig(new JsonObject().put("keys", new JsonArray().add("DATABASE_USER").add("DATABASE_PASSWORD").add("DATABASE_NAME")));
		ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(envStore);
		ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
		retriever.getConfig(r -> {
			String user = r.result().getString("DATABASE_USER");
			String password = r.result().getString("DATABASE_PASSWORD");
			String db = r.result().getString("DATABASE_NAME");
			JsonObject config = new JsonObject();
			LOGGER.info("Connecting {} using {}/{}", db, user, password);
			config.put("connection_string", "mongodb://" + user + ":" + password + "@mongodb/" + db);
			final MongoClient client = MongoClient.createShared(vertx, config);
			final CustomerRepository service = new CustomerRepositoryImpl(client);
			ProxyHelper.registerService(CustomerRepository.class, vertx, service, "customer-service");	
		});
	}

}
