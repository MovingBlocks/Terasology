package org.terasology.metatesting;

import org.junit.jupiter.api.Test;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.naming.Name;
import org.terasology.engine.testUtil.ModuleManagerFactory;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        // This handles dependency resolution and loading in one step, avoiding
        // deprecated getRegistry()
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
        assertEquals(new Name("engine"), providingModule,
                "Should be able to resolve TerasologyEngine class from the environment");

        System.out.println("ModuleLoadingTest passed!");
    }
}
