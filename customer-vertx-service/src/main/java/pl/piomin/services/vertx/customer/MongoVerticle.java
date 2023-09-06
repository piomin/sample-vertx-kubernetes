package pl.piomin.services.vertx.customer;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ProxyHelper;
import pl.piomin.services.vertx.customer.data.CustomerRepository;
import pl.piomin.services.vertx.customer.data.CustomerRepositoryImpl;

public class MongoVerticle extends AbstractVerticle {

    @Override
    public void start() {

        ConfigStoreOptions env = new ConfigStoreOptions()
                .setType("env")
                .setConfig(new JsonObject().put("keys", new JsonArray()
                        .add("MONGO_USERNAME")
                        .add("MONGO_PASSWORD")
                        .add("MONGO_URL")
                        .add("MONGO_DATABASE")));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
                .addStore(env);
        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
        retriever.getConfig().onSuccess(c -> {
            JsonObject config = new JsonObject();
            String url = String.format("mongodb://%s:%s@%s/%s", c.getString("MONGO_USERNAME"), c.getString("MONGO_PASSWORD"),
                    c.getString("MONGO_URL"), c.getString("MONGO_DATABASE"));
            config.put("connection_string", url);
            final MongoClient client = MongoClient.createShared(vertx, config);
            final CustomerRepositoryImpl service = new CustomerRepositoryImpl(client);
            ProxyHelper.registerService(CustomerRepositoryImpl.class, vertx, service, "customer-service");
        });

    }

}
