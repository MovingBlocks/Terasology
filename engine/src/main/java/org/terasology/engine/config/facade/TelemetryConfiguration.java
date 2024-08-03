// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config.facade;

import org.terasology.context.annotation.API;

/**
 * TelemetryConfiguration is a wrapper for {@link org.terasology.engine.config.TelemetryConfig}.
 * It provides other modules with necessary telemetry configuration.
 */
@API
public interface TelemetryConfiguration {

    boolean isTelemetryEnabled();

    boolean isErrorReportingEnabled();

    int fetchBindingSize();

    Boolean get(String telemetryField);

    boolean containsField(String telemetryField);
}
