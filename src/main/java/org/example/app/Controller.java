package org.example.app;

import org.example.framework.annotations.RequestMapping;
import org.example.framework.web.RequestType;

@org.example.framework.annotations.Controller
public class Controller {
    @RequestMapping(path = "/test", method = RequestType.GET)
    public String test() {
        return "test success!";
    }
}
