package org.example;

import org.example.framework.MySpringApplication;
import org.example.framework.web.JSON;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
//        MySpringApplication.start();
        class B implements Serializable {
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
        class A implements Serializable {
            private String fieldA1;
            public int fieldA2;
            public LocalDate date;
            private List<B> b;

            public A(String fieldA1, int fieldA2) {
                this.fieldA1 = fieldA1;
                this.fieldA2 = fieldA2;
                this.date = LocalDate.now();
                this.b = new ArrayList<>();
            }

            public A() {
            }

            @Override
            public String toString() {
                return "A{" +
                        "b=" + b +
                        ", date=" + date +
                        ", fieldA2=" + fieldA2 +
                        ", fieldA1='" + fieldA1 + '\'' +
                        '}';
            }
        }
        A a = new A("string in A", 30);
        B a1 = new B("stringInB1", 41);
        B a2 = new B("stringInB2", 42);
        B a3 = new B("stringInB3", 43);
        a.b.add(a1);
        a.b.add(a2);
        a.b.add(a3);
        String json = JSON.toJson(a);
        System.out.println(json);
        System.out.println();
        System.out.println();
        System.out.println();
        A deserialized = JSON.fromJson(json, a.getClass());
        System.out.println("Deserialized: " + deserialized.toString());

    }


}