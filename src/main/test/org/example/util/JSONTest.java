package org.example.util;

import io.restassured.RestAssured;
import org.example.framework.MySpringApplication;
import org.example.framework.util.JSONSerializer;
import org.example.framework.web.annotations.JsonValue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class JSONTest {
    private static MySpringApplication application;
    private static JSONSerializer jsonSerializer;

    @BeforeAll
    public static void init() {
        RestAssured.baseURI = "http://localhost:8080";
        application = MySpringApplication.start(false);
        jsonSerializer = (JSONSerializer) application.getIoContainer().getBean(JSONSerializer.class);
    }

    @AfterAll
    public static void destroy() {
        application.shutdown();
    }

    @Test
    public void testToJsonSimpleObject() {

        // Create an instance of the class
        Person person = new Person("John", 30);

        // Expected JSON representation
        String expectedJson = "{\"name\":\"John\",\"age\":30}";

        // Test the toJson method
        assertEquals(expectedJson, jsonSerializer.serialize(person));
    }

    @Test
    public void testToJsonIterable() {
        // Create a list of simple objects
        List<String> items = new ArrayList<>();
        items.add("apple");
        items.add("banana");
        items.add("cherry");

        // Expected JSON representation
        String expectedJson = "[\"apple\",\"banana\",\"cherry\"]";

        // Test the toJson method with a collection
        assertEquals(expectedJson, jsonSerializer.serialize(items));
    }

    @Test
    public void testToJsonWithNullField() {
        // Define a class with a null field

        Person person = new Person("John", null);

        String expectedJson = "{\"name\":\"John\",\"age\":null}";
        assertEquals(expectedJson, jsonSerializer.serialize(person));
    }

    @Test
    public void testFromJsonSimpleObject() {
        // Define a simple class
        String json = "{\"name\": \"John\",\"age\": \"30\"}";

        // Deserialize JSON back to an object
        Person person = jsonSerializer.deserialize(json, Person.class);

        assertNotNull(person);
        assertEquals("John", person.name);
        assertEquals(30, person.age);
    }

    @Test
    public void testFromJsonWithInvalidJson() {
        String invalidJson = "{\"name\":\"John\",\"age\":30";

        assertThrows(RuntimeException.class, () -> {
            jsonSerializer.deserialize(invalidJson, Person.class);
        });
    }


    @Test
    public void testToJsonWithComplexField() {
        // Define a class with nested objects
        class Address implements Serializable {
            private String street;
            private String city;

            public Address(String street, String city) {
                this.street = street;
                this.city = city;
            }
        }

        class Person implements Serializable {
            private String name;
            private Address address;

            public Person(String name, Address address) {
                this.name = name;
                this.address = address;
            }
        }

        Address address = new Address("123 Main St", "Anytown");
        Person person = new Person("John", address);

        String expectedJson = "{\"name\":\"John\",\"address\":{\"street\":\"123 Main St\",\"city\":\"Anytown\"}}";

        assertEquals(expectedJson, jsonSerializer.serialize(person));
    }

    @Test
    public void testInvalidClassInToJson() {
        class NotSerializableClass {
            private String name;

            public NotSerializableClass(String name) {
                this.name = name;
            }
        }

        NotSerializableClass obj = new NotSerializableClass("Test");

        assertNull(jsonSerializer.serialize(obj)); // Expecting null since the class is not serializable
    }

    @Test
    public void testFieldResolutionWithAnnotations() {
        // Define a class with annotated fields
        class Person2 implements Serializable {
            @JsonValue("fullName")
            private String name;

            private int age;

            public Person2(String name, int age) {
                this.name = name;
                this.age = age;
            }
        }

        Person2 person = new Person2("John", 30);

        String expectedJson = "{\"fullName\":\"John\",\"age\":30}";
        assertEquals(expectedJson, jsonSerializer.serialize(person));
    }

    @Test
    public void testToJsonEmptyObject() {
        class EmptyClass implements Serializable {
        }

        EmptyClass emptyObject = new EmptyClass();

        String expectedJson = "{}";

        assertEquals(expectedJson, jsonSerializer.serialize(emptyObject));
    }

    @Test
    public void testToJsonWithArray() {
        String[] fruits = {"apple", "banana", "cherry"};

        String expectedJson = "[\"apple\",\"banana\",\"cherry\"]";

        assertEquals(expectedJson, jsonSerializer.serialize(fruits));
    }


    static class Address implements Serializable {
        private String street;
        private String city;

        public Address() {
        }
    }

    static class PersonWithAddress implements Serializable {
        private String name;
        private Address address;

        public PersonWithAddress() {
        }
    }
    @Test
    public void testFromJsonWithNestedObject() {

        String json = "{\"name\":\"John\",\"address\":{\"street\":\"123 Main St\",\"city\":\"Anytown\"}}";

        PersonWithAddress person = jsonSerializer.deserialize(json, PersonWithAddress.class);

        assertNotNull(person);
        assertEquals("John", person.name);
        assertNotNull(person.address);
        assertEquals("123 Main St", person.address.street);
        assertEquals("Anytown", person.address.city);
    }

    @Test
    public void testToJsonWithEmptyCollection() {
        List<String> emptyList = new ArrayList<>();

        String expectedJson = "[]";

        assertEquals(expectedJson, jsonSerializer.serialize(emptyList));
    }


    @Test
    public void testFromJsonWithInvalidType() {
        String json = "{\"name\":\"John\",\"age\":30}";

        assertThrows(RuntimeException.class, () -> {
            List<?> result = jsonSerializer.deserialize(json, List.class); // Trying to deserialize into a List
        });
    }

    @Test
    public void testToJsonWithBooleanField() {
        class Flag implements Serializable {
            private boolean active;

            public Flag(boolean active) {
                this.active = active;
            }
        }

        Flag flag = new Flag(true);

        String expectedJson = "{\"active\":true}";

        assertEquals(expectedJson, jsonSerializer.serialize(flag));
    }

    static class Flag implements Serializable {
        private boolean active;

        public Flag() {
        }
    }
    @Test
    public void testFromJsonWithBooleanField() {

        String json = "{\"active\":true}";

        Flag flag = jsonSerializer.deserialize(json, Flag.class);

        assertNotNull(flag);
        assertTrue(flag.active);
    }

    @Test
    public void testToJsonWithMixedCollection() {
        List<Object> mixedList = new ArrayList<>();
        mixedList.add("text");
        mixedList.add(42);
        mixedList.add(null);
        mixedList.add(true);

        String expectedJson = "[\"text\",42,null,true]";

        assertEquals(expectedJson, jsonSerializer.serialize(mixedList));
    }

    @Test
    public void testFromJsonWithMalformedJson() {
        String malformedJson = "[\"apple\", \"banana\", ";

        assertThrows(RuntimeException.class, () -> {
            List<?> result = jsonSerializer.deserialize(malformedJson, List.class);
        });
    }

    @Test
    public void testToJsonWithDateField() {
        class Event implements Serializable {
            private String name;
            private java.util.Date date;

            public Event(String name, java.util.Date date) {
                this.name = name;
                this.date = date;
            }
        }

        Event event = new Event("Meeting", new java.util.Date(0)); // Epoch time

        String expectedJson = "{\"name\":\"Meeting\",\"date\":\"1970-01-01T00:00:00Z\"}";

        assertEquals(expectedJson, jsonSerializer.serialize(event));
    }

    static class Event implements Serializable {
        private String name;
        private java.util.Date date;

        public Event() {
        }
    }
    @Test
    public void testFromJsonWithDateField() {

        String json = "{\"name\":\"Meeting\",\"date\":\"1970-01-01T00:00:00Z\"}";

        Event event = jsonSerializer.deserialize(json, Event.class);

        assertNotNull(event);
        assertEquals("Meeting", event.name);
        assertEquals(new java.util.Date(0), event.date);
    }
}
