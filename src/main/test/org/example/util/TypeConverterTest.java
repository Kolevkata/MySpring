package org.example.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import org.example.framework.util.type.TypeConverterRegistry;
import org.example.framework.web.RequestType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TypeConverterTest {

    private TypeConverterRegistry typeConverterRegistry;

    @BeforeEach
    void setUp() {
        typeConverterRegistry = new TypeConverterRegistry();
    }
    @AfterEach
    void tearDown() {
        typeConverterRegistry = null;
    }

    @Test
    void testStringToRequestType_validInputs() {
        assertEquals(RequestType.GET, RequestType.fromString("GET"));
        assertEquals(RequestType.POST, RequestType.fromString("POST"));
        assertEquals(RequestType.OPTIONS, RequestType.fromString("OPTIONS"));
        assertEquals(RequestType.DELETE, RequestType.fromString("DELETE"));
        assertEquals(RequestType.PATCH, RequestType.fromString("PATCH"));
    }

    @Test
    void testStringToRequestType_invalidInput() {
        assertThrows(IllegalArgumentException.class, () ->
                RequestType.fromString("INVALID"));
    }

    @Test
    void testMapStringToType_string() {
        Optional<?> result = typeConverterRegistry.convert("\"hello\"", String.class);
        assertTrue(result.isPresent());
        assertEquals("\"hello\"", result.get());
    }

    @Test
    void testMapStringToType_integer() {
        Optional<?> result = typeConverterRegistry.convert("123", Integer.class);
        assertTrue(result.isPresent());
        assertEquals(123, result.get());
    }

    @Test
    void testMapStringToType_long() {
        Optional<?> result = typeConverterRegistry.convert("123456789", Long.class);
        assertTrue(result.isPresent());
        assertEquals(123456789L, result.get());
    }

    @Test
    void testMapStringToType_double() {
        Optional<?> result = typeConverterRegistry.convert("123.45", Double.class);
        assertTrue(result.isPresent());
        assertEquals(123.45, result.get());
    }

    @Test
    void testMapStringToType_bigInteger() {
        Optional<?> result = typeConverterRegistry.convert("123456789012345678", BigInteger.class);
        assertTrue(result.isPresent());
        assertEquals(new BigInteger("123456789012345678"), result.get());
    }

    @Test
    void testMapStringToType_bigDecimal() {
        Optional<?> result = typeConverterRegistry.convert("12345.6789", BigDecimal.class);
        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("12345.6789"), result.get());
    }

    @Test
    void testMapStringToType_boolean() {
        Optional<?> result = typeConverterRegistry.convert("true", Boolean.class);
        assertTrue(result.isPresent());
        assertEquals(true, result.get());
    }

    @Test
    void testMapStringToType_localDate() {
        Optional<?> result = typeConverterRegistry.convert("2023-12-25", LocalDate.class);
        assertTrue(result.isPresent());
        assertEquals(LocalDate.of(2023, 12, 25), result.get());
    }

    @Test
    void testMapStringToType_localDateTime() {
        Optional<?> result = typeConverterRegistry.convert("2023-12-25T15:30:00", LocalDateTime.class);
        assertTrue(result.isPresent());
        assertEquals(LocalDateTime.of(2023, 12, 25, 15, 30, 0), result.get());
    }

    @Test
    void testMapStringToType_date() {
        Optional<?> result = typeConverterRegistry.convert("2023-12-25T15:30:00.000Z", Date.class);
        assertTrue(result.isPresent());
        assertNotNull(result.get());
    }

    @Test
    void testMapStringToType_empty() {
        Optional<?> result = typeConverterRegistry.convert("unsupported", Void.class);
        assertTrue(result.isEmpty());
    }
}
