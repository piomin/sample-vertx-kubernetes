package pl.piomin.services.vertx.customer;

import io.fabric8.openshift.client.OpenShiftClient;
import org.arquillian.cube.openshift.api.Template;
import org.arquillian.cube.openshift.impl.client.OpenShiftAssistant;
import org.arquillian.cube.openshift.impl.enricher.AwaitRoute;
import org.arquillian.cube.openshift.impl.enricher.RouteURL;
import org.arquillian.cube.openshift.impl.requirement.RequiresOpenshift;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.concurrent.TimeUnit;

@Category(RequiresOpenshift.class)
@RequiresOpenshift
@RunWith(ArquillianConditionalRunner.class)
public class CustomerServiceApiTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerServiceApiTest.class);

    @ArquillianResource
    OpenShiftAssistant assistant;
    @ArquillianResource
    OpenShiftClient client;

    @RouteURL("customer-route")
    @AwaitRoute(timeoutUnit = TimeUnit.MINUTES, timeout = 2)
    private URL url;

    @Test
    @Template(url = "classpath:deployment.yaml")
    public void testRoute() {
        LOGGER.info("User: {}", client.currentUser());
        LOGGER.info("Namespace: {}", client.getNamespace());
        client.pods().list().getItems().forEach(p -> LOGGER.info("Pod: {}", p));
        LOGGER.info("Url: ", url.getPath());
    }
}
