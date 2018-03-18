package pl.piomin.services.vertx.customer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.kubernetes.KubernetesServiceImporter;
import pl.piomin.services.vertx.customer.client.AccountClient;
import pl.piomin.services.vertx.customer.data.Customer;
import pl.piomin.services.vertx.customer.data.CustomerRepository;


public class CustomerServer extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerServer.class);
	
	public static void main(String[] args) throws Exception {
		Vertx vertx = Vertx.vertx();
//		vertx.deployVerticle(new MongoVerticle());
		vertx.deployVerticle(new CustomerServer());
	}
	
	@Override
	public void start() throws Exception {
		ServiceDiscovery discovery = ServiceDiscovery.create(vertx);	
		CustomerRepository repository = CustomerRepository.createProxy(vertx, "customer-service");
		  
		Router router = Router.router(vertx);
		router.route("/customer/*").handler(ResponseContentTypeHandler.create());
		router.route(HttpMethod.POST, "/customer").handler(BodyHandler.create());
		router.get("/customer/:id").produces("application/json").handler(rc -> {
			repository.findById(rc.request().getParam("id"), res -> {
				Customer customer = res.result();
				LOGGER.info("Found: {}", customer);
				new AccountClient(discovery).findCustomerAccounts(customer.getId(), res2 -> {
					customer.setAccounts(res2.result());
					rc.response().end(customer.toString());	
				});				
			});
		});
		router.get("/customer/name/:name").produces("application/json").handler(rc -> {
			repository.findByName(rc.request().getParam("name"), res -> {
				List<Customer> customers = res.result();
				LOGGER.info("Found: {}", customers);
				rc.response().end(Json.encodePrettily(customers));
			});
		});
		router.get("/customer").produces("application/json").handler(rc -> {
			repository.findAll(res -> {
				List<Customer> customers = res.result();
				LOGGER.info("Found all: {}", customers);
				rc.response().end(Json.encodePrettily(customers));
			});
		});
		router.post("/customer").produces("application/json").handler(rc -> {
			Customer c = Json.decodeValue(rc.getBodyAsString(), Customer.class);
			repository.save(c, res -> {
				Customer customer = res.result();
				LOGGER.info("Created: {}", customer);
				rc.response().end(customer.toString());
			});
		});
		router.delete("/customer/:id").handler(rc -> {
			repository.remove(rc.request().getParam("id"), res -> {
				LOGGER.info("Removed: {}", rc.request().getParam("id"));
				rc.response().setStatusCode(200);
			});
		});
		
		discovery.registerServiceImporter(new KubernetesServiceImporter(), new JsonObject().put("namespace", "default").put("master", "https://192.168.99.100:8443").put("token", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6ImRlZmF1bHQtdG9rZW4tcDZtZHIiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiZGVmYXVsdCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6ImQwZjljZGRjLTJhOWYtMTFlOC04OWJmLTA4MDAyNzgzYmNjMCIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpkZWZhdWx0OmRlZmF1bHQifQ.N-3vkNz4MYNEgOy0pBBic9EOzRZknLiVOSiwltMg8hM4FDmj-tkNkqUqNczqDARvCJ8mWMVJQpa5H0kEEt7xxkJF8uVk6IuPvHfwZ0e8WYu5krDeUmWE3bruiqbK8-kHLOz-fsH09LvT0ocmBF8m4AZ9-kiIvy3hbTgMfMD-r9fnfvZ04zUB3KSM6-HkBMIcyXEZUWNvDWwo6wYwAhpHyqG8lbmTZyiAiIicDiEUZmh5lvBIFCttTO54zm1pn3Od7I4kEPSkV3mq3n5DnHYQzxcWY7qrI_cE1q-gj9wu9jFSmZGk-0JTKTKFRf-VtrSuNcJiybawsRBHCLxrYHw-2w"));
		vertx.createHttpServer().requestHandler(router::accept).listen(8090);	
		
	}

}
