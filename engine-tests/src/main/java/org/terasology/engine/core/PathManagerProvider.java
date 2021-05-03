// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static org.mockito.Mockito.mock;

public class PathManagerProvider implements ParameterResolver {
    private PathManagerProvider() {};

    static PathManager setPathManager(PathManager pathManager) {
        return PathManager.setInstance(pathManager);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(PathManager.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        ExtensionContext.Namespace namespace = ExtensionContext.Namespace.create(
                getClass(), extensionContext.getRequiredTestInstance());
        PathManager newPathManager = mock(PathManager.class);
        PathManager oldPathManager = setPathManager(newPathManager);

        extensionContext.getStore(namespace).put("oldThing", new Cleaner(oldPathManager, newPathManager));

        return newPathManager;
    }

    static class Cleaner implements ExtensionContext.Store.CloseableResource {
        final PathManager originalPathManager;
        final PathManager tempPathManager;

        Cleaner(PathManager originalPathManager, PathManager tempPathManager) {
            this.originalPathManager = originalPathManager;
            this.tempPathManager = tempPathManager;
        }

        @Override
        public void close() throws Throwable {
            PathManager pathManager = setPathManager(originalPathManager);
            assert pathManager == tempPathManager;
        }
    }
}
