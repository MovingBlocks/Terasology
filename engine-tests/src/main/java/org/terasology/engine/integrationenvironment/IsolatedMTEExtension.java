// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

/**
 * Subclass of {@link MTEExtension} which isolates all test cases by creating a new engine for each test. This is much
 * slower since it runs the startup and shutdown process for all tests. You should use {@link MTEExtension} unless
 * you're certain that you need to use this class.
 * <p>
 * Use this within {@link org.junit.jupiter.api.extension.ExtendWith}
 */
public class IsolatedMTEExtension extends MTEExtension {
    {
        // Resources are not shared between namespaces. We increase isolation by using a different
        // namespace for every test method.
        helperLifecycle = Scopes.PER_METHOD;
    }
}
