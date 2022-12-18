// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment.jupiter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the modules to load in the environment.
 *
 * @deprecated Replace with {@link IntegrationEnvironment#dependencies}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated(since = "5.3.0", forRemoval = true)
public @interface Dependencies {
    /**
     * Names of modules, as defined by the <code>id</code> in their module.txt.
     */
    String[] value();
}
