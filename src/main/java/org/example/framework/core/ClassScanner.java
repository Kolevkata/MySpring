package org.example.framework.core;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class ClassScanner {
    private static final Logger log = Logger.getLogger(ClassScanner.class.getName());
    protected static final Set<String> EXCLUDED_PACKAGES = Set.of(
            "org.codehaus.groovy",
            "groovy",
            "org.junit",
            "org.mockito",
            "org.slf4j"
    );

    protected final String basePackage;
    protected final List<Class<?>> scannedClasses;

    protected ClassScanner(String basePackage) {
        this.basePackage = basePackage;
        this.scannedClasses = new ArrayList<>();
    }

    public static List<Class<?>> scan(String basePackage) {
        return new ClassScanner(basePackage).scanPackage();
    }

    protected List<Class<?>> scanPackage() {
        try {
            String path = basePackage.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = ClassScanner.class.getClassLoader();
            }

            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource != null) {
                    processResource(resource);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scannedClasses;
    }

    private void processResource(URL resource) throws Exception {
        String protocol = resource.getProtocol();
        if ("file".equals(protocol)) {
            scanDirectory(new File(URLDecoder.decode(resource.getFile(), "UTF-8")));
        } else if ("jar".equals(protocol)) {
            scanJar(resource);
        }
    }

    private void scanDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file);
                } else if (file.getName().endsWith(".class")) {
                    processClass(fileToClassName(file));
                }
            }
        }
    }

    private String fileToClassName(File file) {
        String filePath = file.getAbsolutePath();
        String className = filePath.substring(
                filePath.indexOf(basePackage.replace('.', File.separatorChar)),
                filePath.length() - 6  // remove .class
        );
        return className.replace(File.separatorChar, '.');
    }

    private void scanJar(URL resource) throws Exception {
        String jarPath = URLDecoder.decode(resource.getFile(), "UTF-8");
        if (jarPath.contains("!")) {
            jarPath = jarPath.substring(5, jarPath.indexOf("!"));
        }

        try (JarFile jarFile = new JarFile(jarPath)) {
            String packagePath = basePackage.replace('.', '/');
            jarFile.entries().asIterator().forEachRemaining(entry -> {
                if (isClassFile(entry, packagePath)) {
                    String className = entry.getName()
                            .substring(0, entry.getName().length() - 6)
                            .replace('/', '.');
                    processClass(className);
                }
            });
        }
    }

    private boolean isClassFile(JarEntry entry, String packagePath) {
        return entry.getName().startsWith(packagePath) &&
                entry.getName().endsWith(".class");
    }

    protected void processClass(String className) {
        if (EXCLUDED_PACKAGES.stream().anyMatch(className::startsWith)) {
            return;
        }

        try {
            Class<?> clazz = Class.forName(className);
            if (shouldIncludeClass(clazz)) {
                scannedClasses.add(clazz);
            }
        } catch (ClassNotFoundException | LinkageError e) {
            // Skip problematic classes
        }
    }

    protected boolean shouldIncludeClass(Class<?> clazz) {
        return true; // Base implementation includes all classes
    }
}