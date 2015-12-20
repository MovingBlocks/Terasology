
package org.terasology.documentation;

import java.net.URL;

import org.terasology.engine.module.ModuleManager;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.sandbox.API;
import org.terasology.testUtil.ModuleManagerFactory;

/**
 * Enumerates all classes and packages that are annotated with {@link API}.
 */
public class ApiScraper
{
    /**
     * @param args (ignored)
     * @throws Exception if the module environment cannot be loaded
     */
    public static void main(String[] args) throws Exception {
        ModuleManager moduleManager = ModuleManagerFactory.create();
        ModuleEnvironment environment = moduleManager.getEnvironment();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        for (Class<?> apiClass : environment.getTypesAnnotatedWith(API.class)) {
            if (apiClass.isSynthetic()) {
                // This is a package-info
                String pkgName = apiClass.getPackage().getName();
                URL url = classLoader.getResource(pkgName.replace('.', '/'));
                if (url.getProtocol().equals("file")) {
                    System.out.println("Package: " + pkgName);
                }
            } else {
                System.out.println("Class: " + apiClass);
            }
        }
    }
}
