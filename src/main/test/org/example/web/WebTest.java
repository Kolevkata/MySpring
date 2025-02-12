package org.example.web;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.app.dto.A;
import org.example.app.dto.B;
import org.example.framework.MySpringApplication;
import org.example.framework.util.JSONSerializer;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class WebTest {
    private static MySpringApplication application;
    private static JSONSerializer jsonSerializer;

    @BeforeAll
    public static void init() {
        RestAssured.baseURI = "http://localhost:8080";
        application = MySpringApplication.start();
        jsonSerializer = (JSONSerializer) application.getIoContainer().getBean(JSONSerializer.class);
    }

    @AfterAll
    public static void destroy() {
        application.shutdown();
    }

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
                .body(jsonSerializer.serialize(b))
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
                .body(jsonSerializer.serialize(b))
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
                .body(jsonSerializer.serialize(a))
                .when()
                .post("/testCompleteJSON")
                .then()
                .statusCode(200)
                .body(containsString("fieldA1='testA'"))
                .body(containsString("fieldA2=123"));

    }


    @Test
    public void testResponseEntityError() {
        given()
                .when()
                .get("/testReponseEntityError")
                .then()
                .statusCode(400);
    }

    @Test
    public void testResponseEntity() {
        given()
                .when()
                .get("/testReponseEntity")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("value", equalTo("this is object A")) // Match the "value" field
                .body("fieldA2", equalTo(42)) // Match "fieldA2"
                .body("date", equalTo(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))) // Match "date" if relevant
                .body("listOfB.size()", equalTo(2)) // Verify size of "listOfB"
                .body("listOfB[0].fieldB1", equalTo("this is b1")) // Verify first element in "listOfB"
                .body("listOfB[0].fieldB2", equalTo(11)) // Verify first element's fieldB2
                .body("listOfB[1].fieldB1", equalTo("this is b2")) // Verify second element in "listOfB"
                .body("listOfB[1].fieldB2", equalTo(22)); // Verify second element's fieldB2
    }


    @Test
    public void testListBWithListInput() {
        // JSON payload representing a List<B>
        String listOfBJson = """
                [
                    {"fieldB1": "item1", "fieldB2": 10},
                    {"fieldB1": "item2", "fieldB2": 20}
                ]
                """;

        // Perform the POST request and validate the response
        given()
                .contentType(ContentType.JSON)
                .body(listOfBJson)
                .when()
                .post("/listB")
                .then()
                .statusCode(200)
                .body(containsString("listOfB count: 2"))
                .body(containsString("list: {B{fieldB1='item1', fieldB2=10},B{fieldB1='item2', fieldB2=20}}"));
    }

    @Test
    public void testArrBWithArrayInput() {
        // JSON payload representing a B[]
        String arrayOfBJson = """
                [
                    {"fieldB1": "element1", "fieldB2": 30},
                    {"fieldB1": "element2", "fieldB2": 40}
                ]
                """;

        // Perform the POST request and validate the response
        given()
                .contentType(ContentType.JSON)
                .body(arrayOfBJson)
                .when()
                .post("/arrB")
                .then()
                .statusCode(200)
                .body(containsString("listOfB count: 2"))
                .body(containsString("array: [B{fieldB1='element1', fieldB2=30},B{fieldB1='element2', fieldB2=40}]"));
    }

    @Test
    public void testSuccessfulLogin() {
        String requestBody = """
                {
                    "username": "testUser",
                    "password": 123
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body(equalTo("Login successful"));
    }

    @Test
    public void testFailedLogin() {
        String requestBody = """
                {
                    "username": "wrongUser",
                    "password": "wrongPassword"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .statusCode(401)
                .body(equalTo("Login failed"));
    }

    @Test
    public void testGetCurrentUser() {
        // Step 1: Perform Login
        String loginRequestBody = """
                {
                    "username": "testUser",
                    "password": "123"
                }
                """;

        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body(loginRequestBody)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract().response();

        // Step 2: Extract Session or Token from Login Response (if applicable)
        String sessionId = loginResponse.getHeader("Set-Cookie");

        // Step 3: Access `getCurrentUser` Endpoint
        given()
                .contentType(ContentType.JSON)
                .header("Cookie", sessionId) // Pass the session ID or token
                .when()
                .post("/getCurrentUser")
                .then()
                .statusCode(200)
                .body(equalTo("testUser")); // Assuming the user's name is returned
    }

    @Test
    public void testGetAdminWithAdminRole() {
        // Step 1: Perform Login with ROLE_ADMIN
        String loginRequestBody = """
                {
                    "username": "adminUser",
                    "password": "admin"
                }
                """;

        Response loginResponse = given()
                .contentType("application/json")
                .body(loginRequestBody)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract().response();

        // Step 2: Extract Session or Token
        String sessionId = loginResponse.getHeader("Set-Cookie");

        // Step 3: Test /getadmin for ROLE_ADMIN
        given()
                .header("Cookie", sessionId) // Pass the session ID
                .when()
                .post("/getadmin")
                .then()
                .statusCode(200)
                .body(equalTo("adminUser")); // Assuming user's name is returned
    }

    @Test
    public void testGetAdminWithoutAdminRole() {
        // Step 1: Perform Login with a user who doesn't have ROLE_ADMIN
        String loginRequestBody = """
                {
                    "username": "normalUser",
                    "password": "123"
                }
                """;

        Response loginResponse = given()
                .contentType("application/json")
                .body(loginRequestBody)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract().response();

        // Step 2: Extract Session or Token
        String sessionId = loginResponse.getHeader("Set-Cookie");

        // Step 3: Test /getadmin for a non-admin user
        given()
                .header("Cookie", sessionId)
                .when()
                .post("/getadmin")
                .then()
                .statusCode(403); // Access should be denied
    }

    @Test
    public void testAnyWithAdminOrUserRole() {
        // Step 1: Perform Login with ROLE_ADMIN or ROLE_USER
        String loginRequestBody = """
                {
                    "username": "userWithRoles",
                    "password": "123"
                }
                """;

        Response loginResponse = given()
                .contentType("application/json")
                .body(loginRequestBody)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract().response();

        // Step 2: Extract Session or Token
        String sessionId = loginResponse.getHeader("Set-Cookie");

        // Step 3: Test /getadmin for ROLE_ADMIN or ROLE_USER
        given()
                .header("Cookie", sessionId)
                .when()
                .post("/getany")
                .then()
                .statusCode(200)
                .body(equalTo("userWithRoles")); // Assuming user's name is returned
    }
}