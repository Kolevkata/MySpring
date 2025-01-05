package org.example.framework.web;

public class ResponseEntity<T> {
    int status;
    T body;

    public ResponseEntity(int status, T body) {
        this.status = status;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public T getBody() {
        return body;
    }
}