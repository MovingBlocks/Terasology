// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.telemetry;

import org.terasology.gestalt.module.sandbox.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A new metric class show has this annotation.
 * The {@link org.terasology.engine.telemetry.TelemetryScreen} find telemetry information via this annotation.
 */
@API
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TelemetryCategory {
    /**
     * @return The id of the category.
     */
    String id();

    /**
     * @return The displayable name for this category
     */
    String displayName();

    boolean isOneMapMetric();
}
