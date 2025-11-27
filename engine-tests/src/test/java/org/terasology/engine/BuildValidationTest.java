package org.terasology.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;

public class BuildValidationTest {

    @Test
    public void testClasspathSanity() {
        System.out.println("Running BuildValidationTest: Checking classpath sanity...");

        // 1. Check if we can load a standard Java class
        assertNotNull(String.class, "Should be able to load String class");

        // 2. Check if we can find the module.txt for the engine (should be on
        // classpath)
        // Note: In some environments it might be module.json or just in the root.
        // We'll try to find *something* we know exists.
        URL resource = getClass().getResource("/org/terasology/engine/TerasologyEngine.class");
        assertNotNull(resource, "Should be able to find TerasologyEngine class resource");

        System.out.println("Classpath sanity check passed. Found TerasologyEngine at: " + resource);
    }
}
