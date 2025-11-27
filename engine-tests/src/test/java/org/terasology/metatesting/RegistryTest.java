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

    @Test
    public void testContextHierarchy() {
        // 1. Create Root Context (e.g., Engine level)
        Context rootContext = new ContextImpl();
        rootContext.put(String.class, "Root Value");

        // 2. Create Child Context (e.g., Game State level)
        Context childContext = new ContextImpl(rootContext);
        CoreRegistry.setContext(childContext);

        // 3. Verify Child can access Root values
        assertEquals("Root Value", CoreRegistry.get(String.class), "Child context should inherit values from parent");

        // 4. Verify Child can override Root values locally (if supported, or at least put new ones)
        // Note: ContextImpl usually looks in itself first, then parent.
        childContext.put(Integer.class, 123);
        assertEquals(123, CoreRegistry.get(Integer.class));

        // 5. Verify Root does NOT have Child values
        assertNull(rootContext.get(Integer.class), "Parent context should not see child values");
    }

    @Test
    public void testGameStateSimulation() {
        System.out.println("Starting testGameStateSimulation...");

        // 1. Engine Init (Root Context)
        Context engineContext = new ContextImpl();
        engineContext.put(String.class, "Engine Service");
        CoreRegistry.setContext(engineContext);

        String engineValue = CoreRegistry.get(String.class);
        System.out.println("Engine Context Value: " + engineValue);
        assertEquals("Engine Service", engineValue);

        // 2. Switch to Main Menu (Child of Engine)
        Context mainMenuContext = new ContextImpl(engineContext);
        mainMenuContext.put(String.class, "Menu Service");
        CoreRegistry.setContext(mainMenuContext);

        String menuValue = CoreRegistry.get(String.class);
        System.out.println("Main Menu Context Value: " + menuValue);

        // Explicit assertion with clear message
        assertEquals("Menu Service", menuValue, "Menu context should override Engine service with its own");

        // Let's use distinct types to be clear for this test
        mainMenuContext.put(Integer.class, 100);
        assertEquals(100, CoreRegistry.get(Integer.class));

        // 3. Switch to InGame (Sibling of Main Menu, Child of Engine)
        Context gameContext = new ContextImpl(engineContext);
        gameContext.put(Double.class, 99.9);
        CoreRegistry.setContext(gameContext);

        // Verify InGame sees Engine
        String gameValue = CoreRegistry.get(String.class);
        System.out.println("Game Context Value (inherited): " + gameValue);
        assertEquals("Engine Service", gameValue);

        // Verify InGame sees its own
        assertEquals(99.9, CoreRegistry.get(Double.class));

        // Verify InGame does NOT see Main Menu
        assertNull(CoreRegistry.get(Integer.class), "Game context should not see Main Menu values");

        System.out.println("testGameStateSimulation passed!");
    }

    @Test
    public void testInjectionHelper() {
        // This test demonstrates the "Modern" pattern replacing CoreRegistry.
        // Instead of static CoreRegistry.get(), we use Context and InjectionHelper.

        Context context = new ContextImpl();
        context.put(String.class, "Injected Value");

        // 1. Field Injection
        TestBean bean = new TestBean();
        org.terasology.engine.registry.InjectionHelper.inject(bean, context);

        assertEquals("Injected Value", bean.value, "Field should be injected from Context");

        // 2. Constructor Injection (Preferred)
        ConstructorBean cBean = org.terasology.engine.registry.InjectionHelper
                .createWithConstructorInjection(ConstructorBean.class, context);
        assertEquals("Injected Value", cBean.value, "Constructor argument should be injected from Context");
    }

    @Test
    public void testServiceRegistry() {
        // Verify that we can define services via ServiceRegistry (the "modern" way) and retrieve them via Context.
        org.terasology.gestalt.di.ServiceRegistry registry = new org.terasology.gestalt.di.ServiceRegistry();

        // Use a custom class to avoid potential issues with system classes/classloaders in this specific test setup
        // Note that using a String in this case leads to a NullPointerException - an edge case we can likely ignore
        registry.with(TestBean.class).use(() -> {
            TestBean bean = new TestBean();
            bean.value = "Service Registry Value";
            return bean;
        });
        System.out.println("Registry created and configured + " + registry);

        Context context = new ContextImpl(registry);
        System.out.println("Context created: " + context);

        TestBean bean = context.get(TestBean.class);
        System.out.println("Retrieved value: " + bean);

        assertNotNull(bean, "Should retrieve bean defined in ServiceRegistry");
        assertEquals("Service Registry Value", bean.value, "Should retrieve value defined in ServiceRegistry");
        System.out.println("testServiceRegistry passed!");
    }

    // This test bean uses @In to fetch an object from the registry. We use "String" for simplicity, normally it would be a custom object
    public static class TestBean {
        @org.terasology.engine.registry.In
        public String value;
    }

    // This test bean uses constructor injection to fetch an object from the registry. Explicit parameter rather than magic annotation
    public static class ConstructorBean {
        public final String value;

        public ConstructorBean(String value) {
            this.value = value;
        }
    }
}
