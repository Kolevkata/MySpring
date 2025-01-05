package org.example.framework.core;

import org.example.framework.core.annotations.*;
import org.example.framework.security.session.HttpSessionService;
import org.example.framework.security.session.SessionService;
import org.example.framework.web.annotations.Controller;

import javax.xml.stream.util.XMLEventAllocator;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;


public class IOContainer {
    private static final Logger log = Logger.getLogger(IOContainer.class.getName());
    private static final IOContainer instance = new IOContainer();

    public static IOContainer getInstance() {
        return instance;
    }

    private Map<String, Object> beans = new HashMap<>();

    private IOContainer() {
        List<Class<?>> beanClasses = AnnotationClassScanner.scanForAnnotation(Component.class);
        beanClasses.addAll(AnnotationClassScanner.scanForAnnotation(Configuration.class));
        beanClasses.addAll(AnnotationClassScanner.scanForAnnotation(Service.class));
        beanClasses.addAll(AnnotationClassScanner.scanForAnnotation(Controller.class));
        List<Method> beanMethods = MethodScanner.scan("org", Bean.class);

        beanClasses.forEach(clazz -> {
            Object instance = createBean(clazz);
            beans.put(clazz.getName(), instance);
        });

        configureDefaults();

        for (Method beanMethod : beanMethods) {
            Object component = getBean(beanMethod.getDeclaringClass());
            try {
                Object bean = beanMethod.invoke(component);
                beans.put(bean.getClass().getName(), bean);
                //overwrite interfaces
                for (Class<?> anInterface : bean.getClass().getInterfaces()) {
                    beans.put(anInterface.getName(), bean);
                }

            } catch (InvocationTargetException | IllegalAccessException e) {
                log.severe("Cannot register bean from method " + beanMethod.getName() + "." + beanMethod.getDeclaringClass().getName() + " is not a registered component");
                throw new RuntimeException(e);
            }
        }

        // Inject dependencies
        beans.values().forEach(this::injectFieldDependencies);
    }

    private void configureDefaults() {
        beans.put(SessionService.class.getName(), new HttpSessionService());
    }

    private void injectFieldDependencies(Object instance) {
        Class<?> clazz = instance.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object dependency = beans.get(field.getType().getName());
                if (dependency == null) {
                    log.severe("No bean found for dependency: " + field.getType().getName());
                    continue;
                }
                try {
                    field.setAccessible(true);
                    field.set(instance, dependency);
                    System.out.println();
                } catch (IllegalAccessException e) {
                    log.severe("Failed to inject dependency for field: " + field.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    private Object createBean(Class<?> clazz) {
        try {
            // Check for @Inject constructor
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                if (constructor.isAnnotationPresent(Inject.class)) {
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    Object[] params = new Object[paramTypes.length];

                    // Resolve each parameter
                    for (int i = 0; i < paramTypes.length; i++) {
                        String dependencyName = paramTypes[i].getName();
                        Object dependency = beans.get(dependencyName);

                        if (dependency == null) {
                            // Dependency is not yet created, create it now
                            dependency = createBean(paramTypes[i]);
                            beans.put(dependencyName, dependency); // Store the newly created dependency
                        }
                        params[i] = dependency;
                    }
                    return constructor.newInstance(params); // Instantiate the bean with resolved dependencies
                }
            }

            Constructor<?> defaultConstructor = clazz.getConstructor();
            return defaultConstructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean for: " + clazz.getName(), e);
        }
    }

//    public <T> T getBeanAndCast(Class<T> clazz) {
//        Object bean = beans.get(clazz.getName());
//        if (bean == null) {
//            throw new RuntimeException("No bean found for type: " + clazz.getName());
//        }
//        return clazz.cast(bean); // Safely cast the bean to the desired type
//    }

    public Object getBean(Class<?> clazz) {
        Object bean = beans.get(clazz.getName());
        if (bean == null) {
            throw new RuntimeException("No bean found for type: " + clazz.getName());
        }
        return bean;
    }

    public Map<String, Object> getBeans() {
        return beans;
    }

}