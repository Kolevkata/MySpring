package org.example.framework;

import org.example.framework.core.IOContainer;

import java.util.logging.Logger;

public class MySpringApplication {
    private static final Logger log = Logger.getLogger(MySpringApplication.class.getName());

    public static void start() {
        IOContainer ioc = IOContainer.getInstance();
        log.info("Application started");
        ioc.getBean(MyComponent.class).printB();
    }

}