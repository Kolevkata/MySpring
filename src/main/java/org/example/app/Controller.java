package org.example.app;

import org.example.framework.annotations.RequestMapping;

@org.example.framework.annotations.Controller
public class Controller {
    @RequestMapping("/hello")
    public String hello() {
        return "Hello, world!";
    }
}
