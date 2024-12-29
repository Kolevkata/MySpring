package org.example.framework.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class StringUtils {
    private StringUtils() {}

    public static String removeWhiteSpace(String input) {
        StringBuilder output = new StringBuilder();
        boolean insideQuotes = false;

        for (char c : input.toCharArray()) {
            if (c == '"') {
                insideQuotes = !insideQuotes; // Toggle insideQuotes when encountering a quote
            }

            // Append characters, keeping spaces only if inside quotes
            if (!insideQuotes && Character.isWhitespace(c)) {
                continue; // Skip whitespace outside quotes
            }

            output.append(c);
        }
        System.out.println(output);
        return output.toString();
    }


    // More readable solution using Pattern matching
    public static String[] splitPreservingQuotesAndBrackets(String str, char delimiter) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        Stack<Character> brackets = new Stack<>();

        for (char c : str.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
                current.append(c);
            } else if (c == '{' || c == '[') {
                brackets.push(c);
                current.append(c);
            } else if (c == '}' || c == ']') {
                brackets.pop();
                current.append(c);
            } else if (c == delimiter && !inQuotes && brackets.isEmpty()) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            result.add(current.toString().trim());
        }

        return result.toArray(new String[0]);
    }

}
