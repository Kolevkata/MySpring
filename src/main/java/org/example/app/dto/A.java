package org.example.app.dto;

import org.example.framework.web.annotations.JsonValue;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class A implements Serializable {
    @JsonValue(value = "value")
    private String fieldA1;
    public int fieldA2;
    public LocalDate date;
    public List<B> listOfB;

    public A(String fieldA1, int fieldA2) {
        this.fieldA1 = fieldA1;
        this.fieldA2 = fieldA2;
        this.date = LocalDate.now();
        this.listOfB = new ArrayList<>();
    }

    public A() {
    }

    @Override
    public String toString() {
        return "A{" +
                "b=" + listOfB +
                ", date=" + date +
                ", fieldA2=" + fieldA2 +
                ", fieldA1='" + fieldA1 + '\'' +
                '}';
    }
}
