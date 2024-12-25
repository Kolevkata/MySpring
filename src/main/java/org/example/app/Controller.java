package org.example.app;

import org.example.framework.annotations.RequestMapping;
import org.example.framework.web.RequestType;

@org.example.framework.annotations.Controller
public class Controller {
    @RequestMapping(path = "/hello", method = RequestType.GET)
    public String hello() {
        return "get, world!";
    }

    @RequestMapping(path = "/hello", method = RequestType.POST)
    public String helloPost() {
        return "post, world!";
    }
}
