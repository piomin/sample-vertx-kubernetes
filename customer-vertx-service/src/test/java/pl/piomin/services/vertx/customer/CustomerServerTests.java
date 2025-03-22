package pl.piomin.services.vertx.customer;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;
import pl.piomin.services.vertx.customer.data.Customer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(VertxExtension.class)
public class CustomerServerTests {

    final static Logger LOGGER = LoggerFactory.getLogger(CustomerServerTests.class);
    final static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:5.0"));

    @BeforeAll
    static void init(Vertx vertx) {
        mongoDBContainer.start();

        vertx.deployVerticle(new MongoVerticle(mongoDBContainer.getReplicaSetUrl()));
        var deploymentOptions = new DeploymentOptions().setConfig(new JsonObject()
                .put("datasource.uri", mongoDBContainer.getReplicaSetUrl()));
//                .put("datasource.username", DB_USERNAME));
//    deploymentOptions.config = jsonObjectOf(
//                "datasource.uri" to dbUri,
//                "datasource.username" to DB_USERNAME,
//                "datasource.password" to DB_PASSWORD,
//                )
        vertx.deployVerticle(new CustomerServer(), deploymentOptions);
    }

    @AfterAll
    static void destroy() {
        mongoDBContainer.stop();
    }

    @Test
    void shouldFindAll(Vertx vertx, VertxTestContext testContext) {
        HttpClient client = vertx.createHttpClient();
        client.request(HttpMethod.GET, 8080, "localhost", "/customer")
                .compose(req -> req.send().compose(HttpClientResponse::body))
                .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
                    assertNotNull(buffer.toString());
                    LOGGER.info(buffer.toString());
                    testContext.completeNow();
                })));
    }

    @Test
    void shouldAddNew(Vertx vertx, VertxTestContext testContext) {
        Customer c = new Customer();
        c.setAge(20);
        c.setName("Test");
        WebClient client = WebClient.create(vertx);
        client.post(8080, "localhost", "/customer")
                .sendJson(c)
                .onSuccess(res -> {
                    LOGGER.info(res.bodyAsString());
                    assertNotNull(res.body());
                    assertNotNull(res.bodyAsJson(Customer.class).getId());
                    testContext.completeNow();
                });
    }
}
