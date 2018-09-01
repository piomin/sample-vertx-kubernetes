package pl.piomin.services.vertx.customer;

import io.fabric8.openshift.client.OpenShiftClient;
import io.vertx.core.json.Json;
import okhttp3.*;
import org.arquillian.cube.openshift.api.Template;
import org.arquillian.cube.openshift.api.Templates;
import org.arquillian.cube.openshift.impl.client.OpenShiftAssistant;
import org.arquillian.cube.openshift.impl.enricher.AwaitRoute;
import org.arquillian.cube.openshift.impl.enricher.RouteURL;
import org.arquillian.cube.openshift.impl.requirement.RequiresOpenshift;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.piomin.services.vertx.customer.data.Customer;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Category(RequiresOpenshift.class)
@RequiresOpenshift
@RunWith(ArquillianConditionalRunner.class)
@Templates(templates = {
//        @Template(url = "classpath:mongo-deployment.yaml"),
        @Template(url = "classpath:deployment.yaml"),
        @Template(url = "classpath:account-deployment.yaml")
})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CustomerCommunicationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerCommunicationTest.class);

    @ArquillianResource
    OpenShiftAssistant assistant;
    @ArquillianResource
    OpenShiftClient client;

    private static String id;

    @RouteURL(value = "customer-route")
    @AwaitRoute(timeoutUnit = TimeUnit.MINUTES, timeout = 2, path = "/customer")
    private URL url;

    @RouteURL(value = "account-route")
    @AwaitRoute(timeoutUnit = TimeUnit.MINUTES, timeout = 2, path = "/account")
    private URL accountUrl;

    @Test
    public void test1CustomerRoute() {
        LOGGER.info("Route URL: {}", url);
        String projectName = assistant.getCurrentProjectName();
        OkHttpClient httpClient = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), "{\"name\":\"John Smith\", \"age\":33}");
//        Request request = new Request.Builder().url("http://customer-route-" + projectName + ".192.168.99.100.nip.io/customer").post(body).build();
        Request request = new Request.Builder().url(url + "customer").post(body).build();
        try {
            Response response = httpClient.newCall(request).execute();
            ResponseBody b = response.body();
            String json = b.string();
            LOGGER.info("Test: response={}", json);
            Assert.assertNotNull(b);
            Assert.assertEquals(200, response.code());
            Customer c = Json.decodeValue(json, Customer.class);
            this.id = c.getId();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public  void test2AccountRoute() {
        LOGGER.info("Route URL: {}", accountUrl);
        OkHttpClient httpClient = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), "{\"number\":\"01234567890\", \"balance\":10000, \"customerId\":\"" + this.id + "\"}");
        Request request = new Request.Builder().url(accountUrl + "account").post(body).build();
        try {
            Response response = httpClient.newCall(request).execute();
            ResponseBody b = response.body();
            String json = b.string();
            LOGGER.info("Test: response={}", json);
            Assert.assertNotNull(b);
            Assert.assertEquals(200, response.code());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test3GetCustomerWithAccounts() {
        LOGGER.info("Route URL: {}", url);
        LOGGER.info("Get customer: {}", id);
        String projectName = assistant.getCurrentProjectName();
        OkHttpClient httpClient = new OkHttpClient();
//        Request request = new Request.Builder().url("http://customer-route-" + projectName + ".192.168.99.100.nip.io/customer/" + id).get().build();
        Request request = new Request.Builder().url(url + "customer/" + id).get().build();
        try {
            Response response = httpClient.newCall(request).execute();
            LOGGER.info("Test: response={}", response.body().string());
            Assert.assertNotNull(response.body());
            Assert.assertEquals(200, response.code());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
