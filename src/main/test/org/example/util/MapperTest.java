package org.example.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import org.example.framework.util.Mapper;
import org.example.framework.util.TypeConverter;
import org.example.framework.web.RequestType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest {

    @Test
    void testStringToRequestType_validInputs() {
        assertEquals(RequestType.GET, Mapper.stringToRequestType("GET"));
        assertEquals(RequestType.POST, Mapper.stringToRequestType("POST"));
        assertEquals(RequestType.OPTIONS, Mapper.stringToRequestType("OPTIONS"));
        assertEquals(RequestType.DELETE, Mapper.stringToRequestType("DELETE"));
        assertEquals(RequestType.PATCH, Mapper.stringToRequestType("PATCH"));
    }

    @Test
    void testStringToRequestType_invalidInput() {
        assertThrows(IllegalArgumentException.class, () ->
                Mapper.stringToRequestType("INVALID"));
    }

    @Test
    void testMapStringToType_string() {
        Optional<?> result = TypeConverter.convert("\"hello\"", String.class);
        assertTrue(result.isPresent());
        assertEquals("hello", result.get());
    }

    @Test
    void testMapStringToType_integer() {
        Optional<?> result = TypeConverter.convert("123", Integer.class);
        assertTrue(result.isPresent());
        assertEquals(123, result.get());
    }

    @Test
    void testMapStringToType_long() {
        Optional<?> result = TypeConverter.convert("123456789", Long.class);
        assertTrue(result.isPresent());
        assertEquals(123456789L, result.get());
    }

    @Test
    void testMapStringToType_double() {
        Optional<?> result = TypeConverter.convert("123.45", Double.class);
        assertTrue(result.isPresent());
        assertEquals(123.45, result.get());
    }

    @Test
    void testMapStringToType_bigInteger() {
        Optional<?> result = TypeConverter.convert("123456789012345678", BigInteger.class);
        assertTrue(result.isPresent());
        assertEquals(new BigInteger("123456789012345678"), result.get());
    }

    @Test
    void testMapStringToType_bigDecimal() {
        Optional<?> result = TypeConverter.convert("12345.6789", BigDecimal.class);
        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("12345.6789"), result.get());
    }

    @Test
    void testMapStringToType_boolean() {
        Optional<?> result = TypeConverter.convert("true", Boolean.class);
        assertTrue(result.isPresent());
        assertEquals(true, result.get());
    }

    @Test
    void testMapStringToType_localDate() {
        Optional<?> result = TypeConverter.convert("\"2023-12-25\"", LocalDate.class);
        assertTrue(result.isPresent());
        assertEquals(LocalDate.of(2023, 12, 25), result.get());
    }

    @Test
    void testMapStringToType_localDateTime() {
        Optional<?> result = TypeConverter.convert("\"2023-12-25T15:30:00\"", LocalDateTime.class);
        assertTrue(result.isPresent());
        assertEquals(LocalDateTime.of(2023, 12, 25, 15, 30, 0), result.get());
    }

    @Test
    void testMapStringToType_date() {
        Optional<?> result = TypeConverter.convert("\"2023-12-25T15:30:00.000Z\"", Date.class);
        assertTrue(result.isPresent());
        assertNotNull(result.get());
    }

    @Test
    void testMapStringToType_empty() {
        Optional<?> result = TypeConverter.convert("unsupported", Void.class);
        assertTrue(result.isEmpty());
    }
}
