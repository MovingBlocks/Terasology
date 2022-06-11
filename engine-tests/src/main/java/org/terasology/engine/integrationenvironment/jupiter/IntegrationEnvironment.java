// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment.jupiter;

import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.network.NetworkMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegrationEnvironment {
    NetworkMode networkMode() default NetworkMode.NONE;
    Class<? extends EngineSubsystem> subsystem() default NO_SUBSYSTEM.class;

    /**
     * Do not add an extra subsystem to the integration environment.
     * <p>
     * [Odd marker interface because annotation fields cannot default to null.]
     */
    @SuppressWarnings("checkstyle:TypeName")
    abstract class NO_SUBSYSTEM implements EngineSubsystem { }
}
