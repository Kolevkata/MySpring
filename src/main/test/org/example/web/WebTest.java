package org.example.web;

import io.restassured.RestAssured;
import org.example.framework.MySpringApplication;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

public class WebTest {

    @Test
    public void contextLoad() {
        Assertions.assertEquals(true,true);
    }

    @Test
    public void testSuccess() {
        // Perform a GET request to the "/test" endpoint and verify the response
        given()
                .when()
                .get("/test")
                .then()
                .statusCode(200) // Ensure the response status is 200 OK
                .body(equalTo("test success!")); // Verify the body of the response
    }

    @BeforeAll
    public static void init() {
        RestAssured.baseURI = "http://localhost:8080";
        MySpringApplication.start();
    }


    @AfterAll
    public static void destroy() {
        MySpringApplication.shutdown();
    }
}
