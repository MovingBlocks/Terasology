// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment.jupiter;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.integrationenvironment.Engines;
import org.terasology.engine.integrationenvironment.MainLoop;
import org.terasology.engine.integrationenvironment.ModuleTestingHelper;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.registry.In;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.terasology.engine.registry.InjectionHelper.inject;

/**
 * Sets up a Terasology environment for use with your {@index JUnit} 5 test.
 * <p>
 * Supports Terasology's DI as in usual Systems. You can inject Managers via {@link In} annotation, constructors or
 * test parameters. Also you can inject {@link MainLoop} or {@link Engines} to interact with the environment's engine.
 * <p>
 * Example:
 * <pre><code>
 * import org.junit.jupiter.api.Test;
 * import org.terasology.engine.registry.In;
 *
 * &#64;{@link IntegrationEnvironment}(worldGenerator="Pathfinding:pathfinder")
 * public class ExampleTest {
 *
 *     &#64;In
 *     EntityManager entityManager;
 *
 *     &#64;In
 *     {@link MainLoop} mainLoop;
 *
 *     &#64;Test
 *     public void testSomething() {
 *         // …
 *     }
 *
 *     // Injection is also applied to the parameters of individual tests:
 *     &#64;Test
 *     public void testSomething({@link Engines} engines, WorldProvider worldProvider) {
 *         // …
 *     }
 * }
 * </code></pre>
 * <p>
 * See {@link IntegrationEnvironment @IntegrationEnvironment} for information on how to configure the
 * environment's dependencies, world type, and subsystems.
 * <p>
 * By default, JUnit uses a {@link org.junit.jupiter.api.TestInstance.Lifecycle#PER_METHOD PER_METHOD} lifecycle
 * for test instances. The <i>instance</i> of your test class—i.e. {@code this} when your test method executes—
 * is re-created for every {@code @Test} method. The {@link org.terasology.engine.core.GameEngine GameEngine}
 * created by this extension follows the same rules, created for each test instance.
 * <p>
 * If you <em>don't</em> want the engine shut down and recreated for every test method, mark your test class
 * for {@link org.junit.jupiter.api.TestInstance.Lifecycle#PER_CLASS PER_CLASS} lifecycle.
 * <p>
 * Note that classes marked {@link Nested} will share the engine context with their parent.
 *
 * @see <a href="https://junit.org/junit5/docs/current/user-guide/#writing-tests-test-instance-lifecycle"
 *     >JUnit User Guide: Test Instance Lifecycle</a>
 */
public class MTEExtension implements BeforeAllCallback, BeforeEachCallback, ParameterResolver {

    private static final Logger logger = LoggerFactory.getLogger(MTEExtension.class);

    @Override
    public void beforeEach(ExtensionContext extContext) {
        injectTestInstances(extContext);
    }

    @Override
    public void beforeAll(ExtensionContext extContext) {
        extContext.getTestInstance().ifPresentOrElse(
            o -> injectTestInstances(extContext),
            () -> {
                var lifecycle = extContext.getTestInstanceLifecycle().orElse(TestInstance.Lifecycle.PER_METHOD);
                if (!lifecycle.equals(TestInstance.Lifecycle.PER_METHOD)) {
                    logger.warn("Unexpected: This {} test has no instance for {} in context {}",
                            lifecycle, extContext.getUniqueId(), extContext);
                }
            });
    }

    private void injectTestInstances(ExtensionContext extContext) {
        Engines engines = getEngines(extContext);
        // Usually just one instance, but @Nested tests have one per level of nesting.
        for (Object instance : extContext.getRequiredTestInstances().getAllInstances()) {
            injectTestInstance(instance, engines);
        }
    }

    protected void injectTestInstance(Object testInstance, Engines engines) {
        var context = engines.getHostContext();
        context.put(Engines.class, engines);
        context.put(MainLoop.class, new MainLoop(engines));
        context.put(ModuleTestingHelper.class, new ModuleTestingHelper(engines));
        inject(testInstance, context);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (extensionContext.getTestInstance().isEmpty()) {
            logger.debug("Cannot provide parameters to {} before we have a test instance.", parameterContext.getDeclaringExecutable());
            return false;
        }
        Engines engines = getEngines(extensionContext);
        Class<?> type = parameterContext.getParameter().getType();
        return engines.getHostContext().get(type) != null
                || type.isAssignableFrom(Engines.class)
                || type.isAssignableFrom(MainLoop.class)
                || type.isAssignableFrom(ModuleTestingHelper.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Engines engines = getEngines(extensionContext);
        Class<?> type = parameterContext.getParameter().getType();

        return getDIInstance(engines, type);
    }

    private Object getDIInstance(Engines engines, Class<?> type) {
        if (type.isAssignableFrom(Engines.class)) {
            return engines;
        } else if (type.isAssignableFrom(MainLoop.class)) {
            return new MainLoop(engines);
        } else if (type.isAssignableFrom(ModuleTestingHelper.class)) {
            return new ModuleTestingHelper(engines);
        } else {
            return engines.getHostContext().get(type);
        }
    }

    @SuppressWarnings("removal")  // TODO: replace UseWorldGenerator in modules
    public String getWorldGeneratorUri(ExtensionContext context) {
        return findAnnotation(context.getRequiredTestClass(), UseWorldGenerator.class)
                .map(UseWorldGenerator::value)
                .orElseGet(() -> getAnnotationWithDefault(context, IntegrationEnvironment::worldGenerator));
    }

    @SuppressWarnings("removal")  // TODO: replace or remove Dependencies in modules
    public List<String> getDependencyNames(ExtensionContext context) {
        return findAnnotation(context.getRequiredTestClass(), Dependencies.class)
                .map(a -> Arrays.asList(a.value()))
                .orElseGet(() -> Arrays.asList(
                                getAnnotationWithDefault(context, IntegrationEnvironment::dependencies)
                        ));
    }

    public NetworkMode getNetworkMode(ExtensionContext context) {
        return getAnnotationWithDefault(context, IntegrationEnvironment::networkMode);
    }

    public List<Class<? extends EngineSubsystem>> getSubsystems(ExtensionContext context) {
        var subsystem = getAnnotationWithDefault(context, IntegrationEnvironment::subsystem);
        return subsystem.equals(IntegrationEnvironment.NO_SUBSYSTEM.class)
                ? Collections.emptyList() : List.of(subsystem);
    }

    private <T> T getAnnotationWithDefault(ExtensionContext context, Function<IntegrationEnvironment, T> method) {
        var ann =
                findAnnotation(context.getRequiredTestClass(), IntegrationEnvironment.class)
                .orElseGet(ToReadDefaultValuesFrom::getDefaults);
        return method.apply(ann);
    }

    /**
     * Get the Engines for this test.
     * <p>
     * The new Engines instance is configured using the {@link IntegrationEnvironment}
     * annotations for the test class.
     * <p>
     * This will create a new instance when necessary. It will be stored in the
     * {@link ExtensionContext} for reuse between tests that wish to avoid the expense of creating a new
     * instance every time, and will be disposed of when the context closes.
     *
     * @param context for the current test
     * @return configured for this test
     */
    protected Engines getEngines(ExtensionContext context) {
        ExtensionContext.Store store = context.getStore(namespaceFor(context));
        EnginesCleaner autoCleaner = store.getOrComputeIfAbsent(
                EnginesCleaner.class, k -> new EnginesCleaner(
                        getDependencyNames(context),
                        getWorldGeneratorUri(context),
                        getNetworkMode(context),
                        getSubsystems(context)
                ),
                EnginesCleaner.class);
        return autoCleaner.engines;
    }

    protected ExtensionContext.Namespace namespaceFor(ExtensionContext context) {
        logger.debug("Seeking engines for {} : {}", context.getUniqueId(),
                context.getTestInstance().orElse("[NO INSTANCE]"));
        // Start with this Extension, so it's clear where this came from.
        return ExtensionContext.Namespace.create(
                MTEExtension.class,
                context.getRequiredTestInstance()
        );
    }

    /**
     * Manages Engines for storage in an ExtensionContext.
     * <p>
     * Implements {@link ExtensionContext.Store.CloseableResource CloseableResource} to dispose of
     * the {@link Engines} when the context is closed.
     */
    static class EnginesCleaner implements ExtensionContext.Store.CloseableResource {
        protected Engines engines;

        EnginesCleaner(List<String> dependencyNames, String worldGeneratorUri, NetworkMode networkMode,
                       List<Class<? extends EngineSubsystem>> subsystems) {
            engines = new Engines(dependencyNames, worldGeneratorUri, networkMode, subsystems);
            engines.setup();
        }

        @Override
        public void close() {
            engines.tearDown();
            engines = null;
        }
    }

    @IntegrationEnvironment
    private static final class ToReadDefaultValuesFrom {
        static IntegrationEnvironment getDefaults() {
            return ToReadDefaultValuesFrom.class.getDeclaredAnnotation(IntegrationEnvironment.class);
        }
    }
}
