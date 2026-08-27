package org.safa.maintenanceservice;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "PORT=7878",
    "DATABASE_URL=jdbc:postgresql://localhost:5432/image_database",
    "DATABASE_USERNAME=postgres",
    "DATABASE_PASSWORD=18122009",
    "EUREKA_USERNAME=rahim.mustafo.x",
    "EUREKA_PASSWORD=mustafo18122009",
    "EUREKA_URL=192.168.1.200:8761/eureka"
})
class MaintenanceServiceImageServiceApplicationTests {
    @ServiceConnection
    private static final PostgreSQLContainer container = new PostgreSQLContainer("postgres:18");

    @LocalServerPort
    private int port;

    static {
        container.start();
    }

    @BeforeEach
    void configureRestAssured(){
        RestAssured.baseURI="http://localhost";
        RestAssured.port = port;
    }

    @Test
    void contextLoads() {
        RestAssured
                .given()
                .contentType("application/json")
                .when()
                .get("/v1/image/data/98ea3ac2-87fb-4096-95a1-c4d06691be39")
                .then()
                .log()
                .all();
    }

}
