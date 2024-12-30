package org.example.web;
import org.example.framework.annotations.JsonValue;
import org.example.framework.web.JSON;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JSONTest {
    class Person implements Serializable {
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    @Test
    public void testToJsonSimpleObject() {

        // Create an instance of the class
        Person person = new Person("John", 30);

        // Expected JSON representation
        String expectedJson = "{\n\"name\": \"John\",\n\"age\": \"30\"\n}\n";

        // Test the toJson method
        assertEquals(expectedJson, JSON.toJson(person));
    }

    @Test
    public void testToJsonIterable() {
        // Create a list of simple objects
        List<String> items = new ArrayList<>();
        items.add("apple");
        items.add("banana");
        items.add("cherry");

        // Expected JSON representation
        String expectedJson = "[\n\"apple\",\"banana\",\"cherry\"\n]\n";

        // Test the toJson method with a collection
        assertEquals(expectedJson, JSON.toJson(items));
    }

    @Test
    public void testToJsonWithNullField() {
        // Define a class with a null field
        class Person implements Serializable {
            private String name;
            private Integer age;

            public Person(String name, Integer age) {
                this.name = name;
                this.age = age;
            }
        }

        Person person = new Person("John", null);

        String expectedJson = "{\n\"name\": \"John\",\n\"age\": \"null\"\n}\n";

        assertEquals(expectedJson, JSON.toJson(person));
    }

    @Test
    public void testFromJsonSimpleObject() {
        // Define a simple class
        class Person implements Serializable {
            private String name;
            private int age;

            public Person(String name, int age) {
                this.name = name;
                this.age = age;
            }
        }

        String json = "{\n\"name\": \"John\",\n\"age\": \"30\"\n}\n";

        // Deserialize JSON back to an object
        Person person = JSON.fromJson(json, Person.class);

        assertNotNull(person);
        assertEquals("John", person.name);
        assertEquals(30, person.age);
    }

    @Test
    public void testFromJsonWithInvalidJson() {
        String invalidJson = "{\n\"name\": \"John\",\n\"age\": 30\n";

        assertThrows(RuntimeException.class, () -> {
            JSON.fromJson(invalidJson, Person.class);
        });
    }

    @Test
    public void testFromJsonCollection() {
        // Define a class
        class Person implements Serializable {
            private String name;
            private int age;

            public Person(String name, int age) {
                this.name = name;
                this.age = age;
            }
        }

        String json = "[\n{\"name\": \"John\", \"age\": \"30\"},\n{\"name\": \"Jane\", \"age\": \"25\"}\n]";

        // Deserialize JSON array
        List<Person> people = JSON.fromJson(json, List.class);

        assertNotNull(people);
        assertEquals(2, people.size());
        assertEquals("John", people.get(0).name);
        assertEquals(30, people.get(0).age);
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

        String expectedJson = "{\n\"name\": \"John\",\n\"address\": {\"street\": \"123 Main St\",\"city\": \"Anytown\"}\n}\n";

        assertEquals(expectedJson, JSON.toJson(person));
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

        assertNull(JSON.toJson(obj)); // Expecting null since the class is not serializable
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

        String expectedJson = "{\n\"fullName\": \"John\",\n\"age\": \"30\"\n}\n";
        assertEquals(expectedJson, JSON.toJson(person));
    }
}
