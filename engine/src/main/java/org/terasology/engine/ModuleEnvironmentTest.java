package org.terasology.engine;

import org.junit.jupiter.api.Test;
import org.terasology.engine.core.module.ExternalApiAllowlist;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.ModuleRegistry;
import org.terasology.gestalt.module.TableModuleRegistry;
import org.terasology.gestalt.module.dependencyresolution.DependencyResolver;
import org.terasology.gestalt.module.dependencyresolution.ResolutionResult;
import org.terasology.gestalt.naming.Name;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModuleEnvironmentTest {

    @Test
    public void testExternalApiAllowlist() {
        ModuleRegistry registry = new TableModuleRegistry();
        ModuleEnvironment environment = new ModuleEnvironment(registry);
        DependencyResolver resolver = new DependencyResolver(registry);
        ResolutionResult result = resolver.resolve(new Name("engine"));
        assertTrue(result.isSuccess());
        assertTrue(environment.getPermissionProviderFactory().getBasePermissionSet().isPermitted(ExternalApiAllowlist.class));
    }
}
