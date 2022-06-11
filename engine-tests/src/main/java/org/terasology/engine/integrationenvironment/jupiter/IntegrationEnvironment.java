// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment.jupiter;

import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.NetworkMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegrationEnvironment {
    /**
     * The network mode the host engine starts with.
     * <p>
     * See {@link NetworkMode} for details on the options.
     * <p>
     * Some modes automatically include a {@link LocalPlayer}.
     * <p>
     * If you want to simulate multiple players with
     * {@link org.terasology.engine.integrationenvironment.Engines#createClient Engines.createClient},
     * you will need a mode with a {@linkplain NetworkMode#isServer() server}.
     */
    NetworkMode networkMode() default NetworkMode.NONE;

    /**
     * Add an additional subsystem to the engine.
     * <p>
     * A new instance will be included in the engine's subsystems when it is created.
     * <p>
     * Implementing {@link EngineSubsystem#initialise} gives you the opportunity to
     * make changes to the configuration <em>before</em> it would otherwise be available.
     */
    Class<? extends EngineSubsystem> subsystem() default NO_SUBSYSTEM.class;

    /**
     * Do not add an extra subsystem to the integration environment.
     * <p>
     * [Odd marker interface because annotation fields cannot default to null.]
     */
    @SuppressWarnings("checkstyle:TypeName")
    abstract class NO_SUBSYSTEM implements EngineSubsystem { }
}
