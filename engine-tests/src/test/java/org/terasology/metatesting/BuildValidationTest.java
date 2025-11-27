package org.terasology.metatesting;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URL;
import java.util.Enumeration;

public class BuildValidationTest {

    @Test
    public void testClasspathSanity() {
        System.out.println("Running BuildValidationTest: Checking classpath sanity...");

        // 1. Check if we can load a standard Java class
        assertNotNull(String.class, "Should be able to load String class");

        // 2. Check if we can find the TerasologyEngine class
        URL resource = getClass().getResource("/org/terasology/engine/core/TerasologyEngine.class");

        if (resource == null) {
            System.out.println("FATAL: TerasologyEngine class not found!");
            System.out.println("Current ClassLoader: " + getClass().getClassLoader());
            System.out.println("Java Classpath: " + System.getProperty("java.class.path"));
        } else {
            System.out.println("Classpath sanity check passed. Found TerasologyEngine at: " + resource);
        }

        assertNotNull(resource, "Should be able to find TerasologyEngine class resource");

        // 3. Check for module.txt
        // We found it at org/terasology/engine/module.txt in the source
        URL moduleTxt = getClass().getResource("/org/terasology/engine/module.txt");

        if (moduleTxt == null) {
            System.out.println("WARNING: /org/terasology/engine/module.txt not found. Checking for any module.txt...");
            try {
                Enumeration<URL> resources = getClass().getClassLoader().getResources("module.txt");
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    System.out.println("Found a module.txt at: " + url);
                    if (url.toString().contains("engine")) {
                        moduleTxt = url;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Found engine module.txt at: " + moduleTxt);
        }

        assertNotNull(moduleTxt, "Should be able to find module.txt for the engine module");
    }
}
