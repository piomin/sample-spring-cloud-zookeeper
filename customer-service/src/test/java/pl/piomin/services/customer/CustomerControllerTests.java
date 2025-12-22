package pl.piomin.services.customer;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit5.HoverflyExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import pl.piomin.services.customer.model.Customer;
import pl.piomin.services.customer.model.CustomerType;

import java.util.List;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ExtendWith(HoverflyExtension.class)
public class CustomerControllerTests {

    RestTestClient restClient;

    public CustomerControllerTests(WebApplicationContext context) {
        this.restClient = RestTestClient.bindToApplicationContext(context)
                .build();
    }

    @Container
    static GenericContainer zookeeper = new GenericContainer(DockerImageName.parse("zookeeper:3.8"))
            .withExposedPorts(2181);

    @BeforeAll
    static void init() {
        System.setProperty("spring.cloud.zookeeper.connect-string", "localhost:" + zookeeper.getFirstMappedPort());
        System.setProperty("spring.config.import", "optional:zookeeper:localhost:" + zookeeper.getFirstMappedPort());
    }

    @Test
    void findAll() {
        List<Long> ids = List.of(1L, 2L, 3L);
        restClient.post().uri("/ids").body(ids)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Customer[].class)
                .value(customers -> assertEquals(3, customers.length));
    }

    @Test
    void findById() {
        restClient.get().uri("/{id}", 1L)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Customer.class)
                .value(Assertions::assertNotNull)
                .value(customer -> assertNotNull(customer.getId()));
    }

    @Test
    void add() {
        Customer c = new Customer("John Scott", CustomerType.NEW);
        restClient.post().uri("/").body(c)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Customer.class)
                .value(Assertions::assertNotNull)
                .value(customer -> assertNotNull(customer.getId()));
    }

    @Test
    void findByIdWithAccounts(Hoverfly hoverfly) {
        hoverfly.simulate(
                dsl(service("http://account-service")
                        .get("/customer/1")
                        .willReturn(success().body("[{\"id\":\"1\"}]").header("Content-Type", "application/json")))
        );
//        Customer customer = restTemplate.getForObject("/withAccounts/{id}", Customer.class, 1L);
//        assertNotNull(customer);
//        assertNotNull(customer.getId());
//        assertEquals(1, customer.getAccounts().size());
    }

}
