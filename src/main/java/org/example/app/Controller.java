package org.example.app;

import jakarta.servlet.http.HttpServletRequest;
import org.example.app.dto.A;
import org.example.app.dto.B;
import org.example.app.dto.UserDTO;
import org.example.app.service.UserService;
import org.example.framework.core.annotations.Inject;
import org.example.framework.core.annotations.OnDestroy;
import org.example.framework.core.annotations.OnInit;
import org.example.framework.security.session.SessionService;
import org.example.framework.security.user.UserDetails;
import org.example.framework.web.annotations.PathVariable;
import org.example.framework.web.annotations.RequestBody;
import org.example.framework.web.annotations.RequestMapping;
import org.example.framework.web.annotations.RequestParam;
import org.example.framework.web.RequestType;
import org.example.framework.web.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@org.example.framework.web.annotations.Controller
public class Controller {
    @Inject
    UserService userService;

    @RequestMapping(path = "/test", method = RequestType.GET)
    public String test() {
        return "test success!";
    }

    @RequestMapping(path = "/testParam", method = RequestType.GET)
    public String testParam(@RequestParam Long id) {
        return "testParam is " + id;
    }

    @RequestMapping(path = "/testDateParam", method = RequestType.GET)
    public String testParam(@RequestParam LocalDateTime date) {
        return "testParam is " + date;
    }

    @RequestMapping(path = "/testPathVar/{id}", method = RequestType.GET)
    public String testPathVar(@PathVariable Long id) {
        return "testParam is " + id;
    }

    @RequestMapping(path = "/testSimpleJSON", method = RequestType.POST)
    public String testSimpleJSON(@RequestBody B b) {
        return "testBody is " + b.toString();
    }

    @RequestMapping(path = "/testSimpleJSONWithParam", method = RequestType.POST)
    public String testSimpleJSON(@RequestBody B b, @RequestParam(name = "idx") Long id) {
        return "testBody is " + b.toString() + " with id " + id;
    }

    @RequestMapping(path = "/testCompleteJSON", method = RequestType.POST)
    public String testSimpleJSON(@RequestBody A a) {
        return "testBody is " + a.toString();
    }

    @RequestMapping(path = "/testReponseEntity", method = RequestType.GET)
    public ResponseEntity<A> getA() {
        A a = new A("this is object A", 42);
        B b1 = new B("this is b1", 11);
        B b2 = new B("this is b2", 22);
        a.listOfB.add(b1);
        a.listOfB.add(b2);
        return new ResponseEntity<>(200, a);
    }

    @RequestMapping(path = "/testReponseEntityError", method = RequestType.GET)
    public ResponseEntity<A> error() {
        return new ResponseEntity<>(400, null);
    }

    @RequestMapping(path = "/listB", method = RequestType.POST)
    public String listB(@RequestBody List<B> listOfB) {
        return "listOfB count: " + listOfB.size() +
                "list: {" +
                listOfB.stream().map(b -> b.toString()).collect(Collectors.joining(","))
                + "}";
    }

    @RequestMapping(path = "/arrB", method = RequestType.POST)
    public String listB(@RequestBody B[] arrayOfB) {
        return "listOfB count: " + arrayOfB.length +
                "array: [" +
                Arrays.stream(arrayOfB).map(b -> b.toString()).collect(Collectors.joining(","))
                + "]";
    }

    @RequestMapping(path = "/login", method = RequestType.POST)
    public ResponseEntity<String> login(@RequestBody UserDTO user, HttpServletRequest request) {
        boolean login = userService.login(request, user.getUsername(), user.getPassword());
        return (login) ?
                new ResponseEntity<>(200, "Login successful")
                : new ResponseEntity<>(401, "Login failed");
    }

    @RequestMapping(path = "/getCurrentUser", method = RequestType.POST)
    public ResponseEntity<String> login(UserDetails userDetails) {
        System.out.println(userDetails.toString());
        return new ResponseEntity<>(200, userDetails.getName());
    }

}