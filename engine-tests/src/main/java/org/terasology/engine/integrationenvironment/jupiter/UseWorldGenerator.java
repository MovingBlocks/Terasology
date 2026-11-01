// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment.jupiter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares which {@index "world generator"} to use.
 *
 * @deprecated Replace with {@link IntegrationEnvironment#worldGenerator}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated(since = "5.3.0", forRemoval = true)
public @interface UseWorldGenerator {

    /**
     * The URN of the world generator, e.g. <code>"CoreWorlds:facetedPerlin"</code>
     */
    String value();
}
