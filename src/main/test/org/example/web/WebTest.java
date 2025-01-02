package org.example.web;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.example.app.dto.A;
import org.example.app.dto.B;
import org.example.framework.MySpringApplication;
import org.example.framework.util.JSON;
import org.junit.jupiter.api.*;

import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

public class WebTest {

    @Test
    public void contextLoad() {
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

    @Test
    public void testBasicEndpoint() {
        given()
                .when()
                .get("/test")
                .then()
                .statusCode(200)
                .body(equalTo("test success!"));
    }

    @Test
    public void testRequestParam() {
        given()
                .queryParam("id", 123)
                .when()
                .get("/testParam")
                .then()
                .statusCode(200)
                .body(equalTo("testParam is 123"));
    }

    @Test
    public void testDateParam() {
        LocalDateTime date = LocalDateTime.of(2025, 1, 1, 12, 0);
        given()
                .queryParam("date", date.format(DateTimeFormatter.ISO_DATE_TIME))
                .when()
                .get("/testDateParam")
                .then()
                .statusCode(200)
                .body(containsString("2025-01-01T12:00"));
    }

    @Test
    public void testClasspath() {
    }


    @Test
    public void testPathVariable() {
        given()
                .when()
                .get("/testPathVar/456")
                .then()
                .statusCode(200)
                .body(equalTo("testParam is 456"));
    }

    @Test
    public void testSimpleJSON() {
        B b = new B("test", 789);
        given()
                .contentType(ContentType.JSON)
                .body(JSON.toJson(b))
                .when()
                .post("/testSimpleJSON")
                .then()
                .statusCode(200)
                .body(containsString("fieldB1='test'"))
                .body(containsString("fieldB2=789"));
    }

    @Test
    public void testSimpleJSONWithParam() {
        B b = new B("test", 789);
        given()
                .contentType(ContentType.JSON)
                .body(JSON.toJson(b))
                .queryParam("idx", 101)
                .when()
                .post("/testSimpleJSONWithParam")
                .then()
                .statusCode(200)
                .body(containsString("fieldB1='test'"))
                .body(containsString("fieldB2=789"))
                .body(containsString("with id 101"));
    }

    @Test
    public void testCompleteJSON() {
        A a = new A("testA", 123);
        given()
                .contentType(ContentType.JSON)
                .body(JSON.toJson(a))
                .when()
                .post("/testCompleteJSON")
                .then()
                .statusCode(200)
                .body(containsString("fieldA1='testA'"))
                .body(containsString("fieldA2=123"));
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
