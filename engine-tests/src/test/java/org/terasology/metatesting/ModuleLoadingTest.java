package org.terasology.metatesting;

import org.junit.jupiter.api.Test;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.naming.Name;
import org.terasology.engine.testUtil.ModuleManagerFactory;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ModuleLoadingTest {

    @Test
    public void testEngineModuleLoading() throws Exception {
        System.out.println("Running ModuleLoadingTest: Verifying ModuleManager setup...");

        // 1. Create a ModuleManager using the factory (standard way in tests)
        ModuleManager moduleManager = ModuleManagerFactory.create();
        assertNotNull(moduleManager, "ModuleManager should be created successfully");

        // 2. Verify modules are present in the registry
        // TODO: Deprecated getRegistry() for verification/logging is the only way to inspect state? Introduce new way?
        org.terasology.gestalt.module.ModuleRegistry registry = moduleManager.getRegistry();

        System.out.println("Modules in registry: " +
                registry.stream()
                        .map(Module::getId)
                        .map(Name::toString)
                        .collect(Collectors.joining(", ")));

        // 3. Load an environment with just the engine using the recommended API
        // Handles dependency resolution and loading in one step, avoiding the deprecated getRegistry()
        moduleManager.resolveAndLoadEnvironment(new Name("engine"));
        ModuleEnvironment environment = moduleManager.getEnvironment();

        assertNotNull(environment, "Should be able to load an environment with engine module");

        System.out.println("Loaded environment with modules: " +
                environment.getModulesOrderedByDependencies().stream()
                        .map(Module::toString)
                        .collect(Collectors.joining(", ")));

        // 4. Verify we can find a class from the engine module via the environment
        Class<?> expectedClass = org.terasology.engine.core.TerasologyEngine.class;
        Name providingModule = environment.getModuleProviding(expectedClass);
        assertEquals(new Name("engine"), providingModule, "Should be able to resolve TerasologyEngine class from the environment");

        System.out.println("ModuleLoadingTest passed!");
    }

    @Test
    public void testEngineResourceLoading() {
        ModuleManager moduleManager = ModuleManagerFactory.create();
        Module engineModule = moduleManager.getRegistry().getLatestModuleVersion(new Name("engine"));
        assertNotNull(engineModule, "Engine module should be present");

        System.out.println("Engine module classpaths: " + engineModule.getClasspaths());
        System.out.println("Engine module resource roots: " + engineModule.getResources().getRootPaths());

        // Check if module.txt is accessible via ClassLoader directly (sanity check)
        System.out.println("ClassLoader resource (no slash): " +
                ClassLoader.getSystemResource("org/terasology/engine/module.txt"));
        System.out.println("ClassLoader resource (with slash): " +
                ClassLoader.getSystemResource("org/terasology/engine/module.txt/"));

        // The engine module is a Package Module rooted at "org/terasology/engine".
        // So we should ask for "module.txt" relative to that root.
        var moduleTxt = engineModule.getResources().getFile("module.txt");

        if (moduleTxt.isEmpty()) {
            System.out.println("Failed to find module.txt via getFile(\"module.txt\")");
        } else {
            System.out.println("Found module.txt via getFile: " + moduleTxt.get());
        }

        assertTrue(moduleTxt.isPresent(), "module.txt should be present in engine module resources");
    }
}
