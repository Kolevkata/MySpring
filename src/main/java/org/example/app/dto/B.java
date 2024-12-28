package org.example.app.dto;

import java.io.Serializable;

public class B implements Serializable {
    private String fieldB1;
    private int fieldB2;

    public B(String fieldB1, int fieldB2) {
        this.fieldB1 = fieldB1;
        this.fieldB2 = fieldB2;
    }

    public B() {
    }

    @Override
    public String toString() {
        return "B{" +
                "fieldB1='" + fieldB1 + '\'' +
                ", fieldB2=" + fieldB2 +
                '}';
    }
}
