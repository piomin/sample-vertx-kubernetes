package pl.piomin.services.vertx.account;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ProxyHelper;
import pl.piomin.services.vertx.account.data.AccountRepository;
import pl.piomin.services.vertx.account.data.AccountRepositoryImpl;

public class MongoVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
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
            final AccountRepository service = new AccountRepositoryImpl(client);
            ProxyHelper.registerService(AccountRepository.class, vertx, service, "account-service");
        });

    }

}
