// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.terasology.engine.paths.PathManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Utility class for mocking {@link PathManager#getInstance()} method for returning PathManager via ThreadLocal way
 * instead static way.
 * <p>
 * Should be used for parallel testing, when test using PathManager.
 */
public final class PathManagerMocker {

    private static final ThreadLocal<PathManager> PATH_MANAGER_THREAD_LOCAL =
            InheritableThreadLocal.withInitial(PathManagerMocker::createPathManager);
    private static MockedStatic<PathManager> mock;

    private PathManagerMocker() {
        // Utility class
    }

    private static PathManager createPathManager() {
        try {
            Constructor<PathManager> ctor = PathManager.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Cannot create PathManager", e);
        }
    }

    /**
     * Mock {@link PathManager#getInstance()}.
     * <p>
     * AHTUNG! This method using {@code black magic} for JVM bytecode manipulation.
     * <p>
     * Redirects calls to {@link #redirectedGetInstance(InvocationOnMock)}
     */
    public static void mockPathManager() {
        if (mock == null) {
            mock = Mockito.mockStatic(PathManager.class, PathManagerMocker::redirectedGetInstance);
        }
    }

    /**
     * Resets `PathManager` in ThreadLocal.
     */
    public static void resetPathManager() {
        PATH_MANAGER_THREAD_LOCAL.remove();
    }

    private static Object redirectedGetInstance(InvocationOnMock invocationOnMock) throws Throwable {
        if (invocationOnMock.getMethod().getName().equals("getInstance")) {
            return PATH_MANAGER_THREAD_LOCAL.get();
        }
        return invocationOnMock.callRealMethod();
    }
}
