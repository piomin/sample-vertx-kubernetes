package pl.piomin.services.vertx.account;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ServiceBinder;
import pl.piomin.services.vertx.account.data.AccountRepository;
import pl.piomin.services.vertx.account.data.AccountRepositoryImpl;

public class MongoVerticle extends AbstractVerticle {

    private String url;

    public MongoVerticle() {
    }

    public MongoVerticle(String url) {
        this.url = url;
    }

    @Override
    public void start() throws Exception {

        if (url != null) {
            createMongo(url);
            return;
        }

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
            String url = String.format("mongodb://%s:%s@%s/%s", c.getString("MONGO_USERNAME"), c.getString("MONGO_PASSWORD"),
                    c.getString("MONGO_URL"), c.getString("MONGO_DATABASE"));
            createMongo(url);
        });

    }

    private void createMongo(String url) {
        JsonObject config = new JsonObject();
        config.put("connection_string", url);
        final MongoClient client = MongoClient.createShared(vertx, config);
        final AccountRepository service = new AccountRepositoryImpl(client);
        new ServiceBinder(vertx).setAddress("account-service").register(AccountRepository.class, service);
    }

}
