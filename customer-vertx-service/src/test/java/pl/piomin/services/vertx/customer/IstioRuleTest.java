package pl.piomin.services.vertx.customer;

import io.vertx.core.json.Json;
import okhttp3.*;
import org.arquillian.cube.istio.api.IstioResource;
import org.arquillian.cube.istio.impl.IstioAssistant;
import org.arquillian.cube.openshift.api.Template;
import org.arquillian.cube.openshift.api.Templates;
import org.arquillian.cube.openshift.impl.client.OpenShiftAssistant;
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
import java.net.MalformedURLException;
import java.net.URL;

@Category(RequiresOpenshift.class)
@RequiresOpenshift
@Templates(templates = {
        @Template(url = "classpath:account-deployment.yaml"),
        @Template(url = "classpath:deployment.yaml")
})
@RunWith(ArquillianConditionalRunner.class)
@IstioResource("classpath:customer-to-account-route.yaml")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IstioRuleTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(IstioRuleTest.class);
    private static String id;

    @ArquillianResource
    private IstioAssistant istioAssistant;
    @ArquillianResource
    OpenShiftAssistant openShiftAssistant;

    @RouteURL(value = "customer-route", path = "/customer")
    private URL customerUrl;
    @RouteURL(value = "account-route", path = "/account")
    private URL accountUrl;

    @Test
    public void test1CustomerRoute() {
        LOGGER.info("URL: {}", customerUrl);
        istioAssistant.await(customerUrl, r -> r.isSuccessful());
        LOGGER.info("URL ready. Proceeding to the test");
        OkHttpClient httpClient = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), "{\"name\":\"John Smith\", \"age\":33}");
        Request request = new Request.Builder().url(customerUrl).post(body).build();
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
        istioAssistant.await(accountUrl, r -> r.isSuccessful());
        LOGGER.info("URL ready. Proceeding to the test");
        OkHttpClient httpClient = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), "{\"number\":\"01234567890\", \"balance\":10000, \"customerId\":\"" + this.id + "\"}");
        Request request = new Request.Builder().url(accountUrl).post(body).build();
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
        String url = customerUrl + "/" + id;
        LOGGER.info("Calling URL: {}", customerUrl);
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();
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
