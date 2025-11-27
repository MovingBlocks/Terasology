package org.terasology.metatesting;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.registry.CoreRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class RegistryTest {

    private Context originalContext;

    @BeforeEach
    public void setUp() {
        // Save original context to restore after test
        originalContext = CoreRegistry.get(Context.class);
        // Clear registry for test
        CoreRegistry.setContext(null);
    }

    @AfterEach
    public void tearDown() {
        // Restore original context
        CoreRegistry.setContext(originalContext);
    }

    @Test
    public void testBasicRegistryOperations() {
        Context context = new ContextImpl();
        CoreRegistry.setContext(context);

        String testString = "Test String";
        CoreRegistry.put(String.class, testString);

        String retrieved = CoreRegistry.get(String.class);
        assertNotNull(retrieved, "Should retrieve object from registry");
        assertEquals(testString, retrieved, "Retrieved object should match put object");
        assertSame(testString, retrieved, "Retrieved object should be the same instance");
    }

    @Test
    public void testContextIsolation() {
        // Context 1
        Context context1 = new ContextImpl();
        CoreRegistry.setContext(context1);
        CoreRegistry.put(String.class, "Value 1");
        assertEquals("Value 1", CoreRegistry.get(String.class));

        // Context 2
        Context context2 = new ContextImpl();
        CoreRegistry.setContext(context2);
        assertNull(CoreRegistry.get(String.class), "New context should be empty");

        CoreRegistry.put(String.class, "Value 2");
        assertEquals("Value 2", CoreRegistry.get(String.class));

        // Switch back to Context 1
        CoreRegistry.setContext(context1);
        assertEquals("Value 1", CoreRegistry.get(String.class), "Should preserve values in original context");
    }

    @Test
    public void testGetContext() {
        Context context = new ContextImpl();
        CoreRegistry.setContext(context);

        Context retrievedContext = CoreRegistry.get(Context.class);
        assertSame(context, retrievedContext, "CoreRegistry.get(Context.class) should return the current context");
    }
}
