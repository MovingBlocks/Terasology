// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import com.google.common.collect.ObjectArrays;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.terasology.engine.integrationenvironment.jupiter.MTEExtension;

import java.util.function.Function;

public final class Scopes {

    /** One per top-level test class. */
    public static final Function<ExtensionContext, ExtensionContext.Namespace> PER_CLASS = context -> mteNamespace(getTopTestClass(context));

    /** One per test method. */
    public static final Function<ExtensionContext, ExtensionContext.Namespace> PER_METHOD = context -> mteNamespace(context.getTestMethod());

    private Scopes() { };

    static ExtensionContext.Namespace mteNamespace(Object... parts) {
        // Start with this Extension, so it's clear where this came from.
        return ExtensionContext.Namespace.create(ObjectArrays.concat(MTEExtension.class, parts));
    }

    /**
     * The outermost class defining this test.
     * <p>
     * For <a href="https://junit.org/junit5/docs/current/user-guide/#writing-tests-nested">nested tests</a>, this
     * returns the outermost class in which this test is nested.
     * <p>
     * Most tests are not nested, in which case this returns the class defining the test.
     *
     * @param context for the current test
     * @return a test class
     */
    public static Class<?> getTopTestClass(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        return testClass.isAnnotationPresent(Nested.class) ? testClass.getEnclosingClass() : testClass;
    }
}
