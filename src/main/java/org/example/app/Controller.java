package org.example.app;

import org.example.app.dto.A;
import org.example.app.dto.B;
import org.example.framework.annotations.PathVariable;
import org.example.framework.annotations.RequestBody;
import org.example.framework.annotations.RequestMapping;
import org.example.framework.annotations.RequestParam;
import org.example.framework.web.RequestType;

@org.example.framework.annotations.Controller
public class Controller {
    @RequestMapping(path = "/hello", method = RequestType.GET)
    public String hello(@RequestParam() String a) {
        return "get, world! " + a.toString();
    }
    @RequestMapping(path = "/hello/{id}", method = RequestType.GET)
    public String hello(@PathVariable Long id) {
        return "get, world! id is:" + id;
    }

    @RequestMapping(path = "/hello", method = RequestType.POST)
    public String helloPost() {
        return "post, world!";
    }
}
