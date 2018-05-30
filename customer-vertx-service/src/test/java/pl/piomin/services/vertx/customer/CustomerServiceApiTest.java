package pl.piomin.services.vertx.customer;

import io.fabric8.openshift.client.OpenShiftClient;
import org.arquillian.cube.openshift.impl.client.OpenShiftAssistant;
import org.arquillian.cube.openshift.impl.requirement.RequiresOpenshift;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(RequiresOpenshift.class)
@RequiresOpenshift
@RunWith(ArquillianConditionalRunner.class)
public class CustomerServiceApiTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerServiceApiTest.class);

    @ArquillianResource
    OpenShiftAssistant assistant;
    @ArquillianResource
    OpenShiftClient client;

    @Test
    public void testRoute() {
        LOGGER.info("Current namespace: {}", assistant.findProject("sample-deployment").get());
        LOGGER.info("User: {}", client.currentUser());
    }
}
