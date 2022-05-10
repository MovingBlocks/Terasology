// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.opentest4j.MultipleFailuresError;
import org.slf4j.LoggerFactory;
import org.terasology.engine.integrationenvironment.extension.Dependencies;
import org.terasology.engine.integrationenvironment.extension.UseWorldGenerator;
import org.terasology.engine.registry.In;
import org.terasology.unittest.worlds.DummyWorldGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Sets up a Terasology environment for use with your {@index JUnit} 5 test.
 * <p>
 * Supports Terasology's DI as in usual Systems. You can inject Managers via {@link In} annotation, constructors or
 * test parameters. Also you can inject {@link MainLoop} or {@link Engines} to interact with the environment's engine.
 * <p>
 * Example:
 * <pre><code>
 * import org.junit.jupiter.api.extension.ExtendWith;
 * import org.junit.jupiter.api.Test;
 * import org.terasology.engine.registry.In;
 *
 * &#64;{@link org.junit.jupiter.api.extension.ExtendWith}(MTEExtension.class)
 * &#64;{@link Dependencies}("MyModule")
 * &#64;{@link UseWorldGenerator}("Pathfinding:pathfinder")
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
 * You can configure the environment with these additional annotations:
 * <dl>
 *     <dt>{@link Dependencies @Dependencies}</dt>
 *     <dd>Specify which modules to include in the environment. Put the name of your module under test here.
 *         Any dependencies these modules declare in <code>module.txt</code> will be pulled in as well.</dd>
 *     <dt>{@link UseWorldGenerator @UseWorldGenerator}</dt>
 *     <dd>The URN of the world generator to use. Defaults to {@link DummyWorldGenerator},
 *         a flat world.</dd>
 * </dl>
 *
 * <p>
 * Every class annotated with this will create a single instance of {@link Engines} and use it during execution of
 * all tests in the class. This also means that all engine instances are shared between all tests in the class. If you
 * want isolated engine instances try {@link IsolatedMTEExtension}.
 * <p>
 * Note that classes marked {@link Nested} will share the engine context with their parent.
 * <p>
 * This will configure the logger and the current implementation is not subtle or polite about it, see
 * {@link #setupLogging()} for notes.
 */
public class MTEExtension implements BeforeAllCallback, ParameterResolver, TestInstancePostProcessor {

    static final String LOGBACK_RESOURCE = "default-logback.xml";
    protected Function<ExtensionContext, ExtensionContext.Namespace> helperLifecycle = Scopes.PER_CLASS;
    protected Function<ExtensionContext, Class<?>> getTestClass = Scopes::getTopTestClass;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (context.getRequiredTestClass().isAnnotationPresent(Nested.class)) {
            return;  // nested classes get set up in the parent
        }
        setupLogging();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        Engines engines = getEngines(extensionContext);
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

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) {
        Engines engines = getEngines(extensionContext);
        List<IllegalAccessException> exceptionList = new LinkedList<>();
        Class<?> type = testInstance.getClass();
        while (type != null) {
            Arrays.stream(type.getDeclaredFields())
                    .filter((field) -> field.getAnnotation(In.class) != null)
                    .peek((field) -> field.setAccessible(true))
                    .forEach((field) -> {
                        Object candidateObject = getDIInstance(engines, field.getType());
                        try {
                            field.set(testInstance, candidateObject);
                        } catch (IllegalAccessException e) {
                            exceptionList.add(e);
                        }
                    });

            type = type.getSuperclass();
        }
        // It is tests, then it is legal ;)
        if (!exceptionList.isEmpty()) {
            throw new MultipleFailuresError("I cannot provide DI instances:", exceptionList);
        }
    }

    public String getWorldGeneratorUri(ExtensionContext context) {
        UseWorldGenerator useWorldGenerator = getTestClass.apply(context).getAnnotation(UseWorldGenerator.class);
        return useWorldGenerator != null ? useWorldGenerator.value() : null;
    }

    public Set<String> getDependencyNames(ExtensionContext context) {
        Dependencies dependencies = getTestClass.apply(context).getAnnotation(Dependencies.class);
        return dependencies != null ? Sets.newHashSet(dependencies.value()) : Collections.emptySet();
    }

    /**
     * Get the Engines for this test.
     * <p>
     * The new Engines instance is configured using the {@link Dependencies} and {@link UseWorldGenerator}
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
        ExtensionContext.Store store = context.getStore(helperLifecycle.apply(context));
        EnginesCleaner autoCleaner = store.getOrComputeIfAbsent(
                EnginesCleaner.class, k -> new EnginesCleaner(getDependencyNames(context), getWorldGeneratorUri(context)),
                EnginesCleaner.class);
        return autoCleaner.engines;
    }

    /**
     * Apply our default logback configuration to the logger.
     * <p>
     * Modules won't generally have their own logback-test.xml, so we'll install ours from {@value LOGBACK_RESOURCE}.
     * <p>
     * <b>TODO:</b>
     * <ul>
     *   <li>Only reset the current LoggerContext if it really hasn't been customized by elsewhere.
     *   <li>When there are multiple classes with MTEExtension, do we end up doing this repeatedly
     *       in the same process?
     *   <li>Provide a way to add/change/override what this is doing that doesn't require checking
     *       out the MTE sources and editing default-logback.xml.
     * </ul>
     */
    void setupLogging() {
        // This is mostly right out of the book:
        //   http://logback.qos.ch/xref/chapters/configuration/MyApp3.html
        JoranConfigurator cfg = new JoranConfigurator();
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        cfg.setContext(context);
        try (InputStream i = getClass().getResourceAsStream(LOGBACK_RESOURCE)) {
            if (i == null) {
                throw new RuntimeException("Failed to find " + LOGBACK_RESOURCE);
            }
            cfg.doConfigure(i);
        } catch (IOException e) {
            throw new RuntimeException("Error reading " + LOGBACK_RESOURCE, e);
        } catch (JoranException e) {
            throw new RuntimeException("Error during logger configuration", e);
        } finally {
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        }
    }

    /**
     * Manages Engines for storage in an ExtensionContext.
     * <p>
     * Implements {@link ExtensionContext.Store.CloseableResource CloseableResource} to dispose of
     * the {@link Engines} when the context is closed.
     */
    static class EnginesCleaner implements ExtensionContext.Store.CloseableResource {
        protected Engines engines;

        EnginesCleaner(Set<String> dependencyNames, String worldGeneratorUri) {
            engines = new Engines(dependencyNames, worldGeneratorUri);
            engines.setup();
        }

        @Override
        public void close() {
            engines.tearDown();
            engines = null;
        }
    }
}
