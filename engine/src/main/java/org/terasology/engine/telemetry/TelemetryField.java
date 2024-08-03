// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.telemetry;

import org.terasology.context.annotation.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * All telemetry fields should have this annotation.
 * {@link org.terasology.engine.telemetry.metrics.Metric} finds all telemetry fields and values via this annotation.
 */
@API
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TelemetryField {
}
