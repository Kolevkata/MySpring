package org.example.util;

import org.example.framework.util.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilsTest {
    @Test
    public void testRemoveWhiteSpaceNoQuotes() {
        String input = "Hello World!";
        String expected = "HelloWorld!";
        assertEquals(expected, StringUtils.removeWhiteSpace(input));
    }

    @Test
    public void testRemoveWhiteSpaceWithQuotes() {
        String input = "Hello \"World Foo\" Bar";
        String expected = "Hello\"World Foo\"Bar";
        assertEquals(expected, StringUtils.removeWhiteSpace(input));
    }

    @Test
    public void testRemoveWhiteSpaceOnlyQuotes() {
        String input = "\"Hello World\" \"Foo Bar\"";
        String expected = "\"Hello World\"\"Foo Bar\"";
        assertEquals(expected, StringUtils.removeWhiteSpace(input));
    }

    @Test
    public void testRemoveWhiteSpaceEmptyInput() {
        String input = "";
        String expected = "";
        assertEquals(expected, StringUtils.removeWhiteSpace(input));
    }

    @Test
    public void testRemoveWhiteSpaceSpacesOnly() {
        String input = "   ";
        String expected = "";
        assertEquals(expected, StringUtils.removeWhiteSpace(input));
    }

    @Test
    public void testRemoveWhiteSpaceWithLeadingAndTrailingSpaces() {
        String input = "  Hello   World  ";
        String expected = "HelloWorld";
        assertEquals(expected, StringUtils.removeWhiteSpace(input));
    }

    @Test
    public void testSplitPreservingQuotesAndBracketsNoQuotesOrBrackets() {
        String input = "a,b,c";
        String[] expected = {"a", "b", "c"};
        assertArrayEquals(expected, StringUtils.splitPreservingQuotesAndBrackets(input, ','));
    }

    @Test
    public void testSplitPreservingQuotesAndBracketsWithQuotes() {
        String input = "a,\"b,c\",d";
        String[] expected = {"a", "\"b,c\"", "d"};
        assertArrayEquals(expected, StringUtils.splitPreservingQuotesAndBrackets(input, ','));
    }

    @Test
    public void testSplitPreservingQuotesAndBracketsWithBrackets() {
        String input = "a,[b,c],d";
        String[] expected = {"a", "[b,c]", "d"};
        assertArrayEquals(expected, StringUtils.splitPreservingQuotesAndBrackets(input, ','));
    }

    @Test
    public void testSplitPreservingQuotesAndBracketsWithNestedBrackets() {
        String input = "a,{b,[c,d]},e";
        String[] expected = {"a", "{b,[c,d]}", "e"};
        assertArrayEquals(expected, StringUtils.splitPreservingQuotesAndBrackets(input, ','));
    }

    @Test
    public void testSplitPreservingQuotesAndBracketsEmptyInput() {
        String input = "";
        String[] expected = {};
        assertArrayEquals(expected, StringUtils.splitPreservingQuotesAndBrackets(input, ','));
    }

    @Test
    public void testSplitPreservingQuotesAndBracketsWithSpaces() {
        String input = "a,   ,b ,  c";
        String[] expected = {"a", "", "b", "c"};
        assertArrayEquals(expected, StringUtils.splitPreservingQuotesAndBrackets(input, ','));
    }
}
