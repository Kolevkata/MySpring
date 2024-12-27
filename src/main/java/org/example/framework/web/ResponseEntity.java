package org.example.framework.web;

public class ResponseEntity<T> {
    private int statusCode;
    private String responseBody;

    public ResponseEntity(int statusCode) {
        this.statusCode = statusCode;
    }

    public ResponseEntity(int statusCode, String responseBody) {
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
}
