package org.example.web;
import org.example.framework.annotations.JsonValue;
import org.example.framework.web.JSON;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class JSONTest {

    @Test
    public void testToJsonSimpleObject() {

        // Create an instance of the class
        Person person = new Person("John", 30);

        // Expected JSON representation
        String expectedJson = "{\"name\":\"John\",\"age\":\"30\"}";

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
        String expectedJson = "[\"apple\",\"banana\",\"cherry\"]";

        // Test the toJson method with a collection
        assertEquals(expectedJson, JSON.toJson(items));
    }

    @Test
    public void testToJsonWithNullField() {
        // Define a class with a null field

        Person person = new Person("John", null);

        String expectedJson = "{\"name\":\"John\",\"age\":null}";
        assertEquals(expectedJson, JSON.toJson(person));
    }

    @Test
    public void testFromJsonSimpleObject() {
        // Define a simple class
        String json = "{\"name\": \"John\",\"age\": \"30\"}";

        // Deserialize JSON back to an object
        Person person = JSON.fromJson(json, Person.class);

        assertNotNull(person);
        assertEquals("John", person.name);
        assertEquals(30, person.age);
    }

    @Test
    public void testFromJsonWithInvalidJson() {
        String invalidJson = "{\"name\":\"John\",\"age\":30";

        assertThrows(RuntimeException.class, () -> {
            JSON.fromJson(invalidJson, Person.class);
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

        String expectedJson = "{\"fullName\":\"John\",\"age\":\"30\"}";
        assertEquals(expectedJson, JSON.toJson(person));
    }

    @Test
    public void testToJsonEmptyObject() {
        class EmptyClass implements Serializable {
        }

        EmptyClass emptyObject = new EmptyClass();

        String expectedJson = "{}";

        assertEquals(expectedJson, JSON.toJson(emptyObject));
    }

    @Test
    public void testToJsonWithArray() {
        String[] fruits = {"apple", "banana", "cherry"};

        String expectedJson = "[\"apple\",\"banana\",\"cherry\"]";

        assertEquals(expectedJson, JSON.toJson(fruits));
    }

    @Test
    public void testFromJsonWithNestedObject() {
        class Address implements Serializable {
            private String street;
            private String city;

            public Address() {
            }
        }

        class Person implements Serializable {
            private String name;
            private Address address;

            public Person() {
            }
        }

        String json = "{\"name\":\"John\",\"address\":{\"street\":\"123 Main St\",\"city\":\"Anytown\"}}";

        Person person = JSON.fromJson(json, Person.class);

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

        assertEquals(expectedJson, JSON.toJson(emptyList));
    }

    @Test
    public void testFromJsonWithEmptyCollection() {
        String json = "[]";

        List<?> result = JSON.fromJson(json, List.class);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFromJsonWithInvalidType() {
        String json = "{\"name\":\"John\",\"age\":30}";

        assertThrows(ClassCastException.class, () -> {
            List<?> result = JSON.fromJson(json, List.class); // Trying to deserialize into a List
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

        assertEquals(expectedJson, JSON.toJson(flag));
    }

    @Test
    public void testFromJsonWithBooleanField() {
        class Flag implements Serializable {
            private boolean active;

            public Flag() {
            }
        }

        String json = "{\"active\":true}";

        Flag flag = JSON.fromJson(json, Flag.class);

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

        assertEquals(expectedJson, JSON.toJson(mixedList));
    }

    @Test
    public void testFromJsonWithMalformedJson() {
        String malformedJson = "[\"apple\", \"banana\", ";

        assertThrows(RuntimeException.class, () -> {
            List<?> result = JSON.fromJson(malformedJson, List.class);
        });
    }

    @Test
    public void testToJsonWithCircularReference() {
        class Node implements Serializable {
            private String name;
            private Node next;

            public Node(String name) {
                this.name = name;
            }

            public void setNext(Node next) {
                this.next = next;
            }
        }

        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        nodeA.setNext(nodeB);
        nodeB.setNext(nodeA); // Circular reference

        assertThrows(RuntimeException.class, () -> {
            JSON.toJson(nodeA); // Should fail due to circular reference
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

        assertEquals(expectedJson, JSON.toJson(event));
    }

    @Test
    public void testFromJsonWithDateField() {
        class Event implements Serializable {
            private String name;
            private java.util.Date date;

            public Event() {
            }
        }

        String json = "{\"name\":\"Meeting\",\"date\":\"1970-01-01T00:00:00Z\"}";

        Event event = JSON.fromJson(json, Event.class);

        assertNotNull(event);
        assertEquals("Meeting", event.name);
        assertEquals(new java.util.Date(0), event.date);
    }
}
