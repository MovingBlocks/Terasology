// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.terasology.engine.config.PlayerConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.TerasologyEngine;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.integrationenvironment.jupiter.MTEExtension;

import static com.google.common.truth.Truth.assertThat;
import static org.terasology.engine.testUtil.Correspondences.instanceOfExpected;

@Tag("MteTest")
@ExtendWith(MTEExtension.class)
@IntegrationEnvironment(subsystem = CustomSubsystemTest.MySubsystem.class)
public class CustomSubsystemTest {

    static final String PLAYER_NAME = "Customized Name Just For This";

    @Test
    void testSubsystemExists(GameEngine engine) {
        assertThat(((TerasologyEngine) engine).getSubsystems())
                .comparingElementsUsing(instanceOfExpected())
                .contains(MySubsystem.class);
    }

    @Test
    void testConfigurationBySubsystemInitialisation(PlayerConfig config) {
        assertThat(config.playerName.get()).isEqualTo(PLAYER_NAME);
    }

    /**
     * Configure the name of the player.
     * <p>
     * The subsystem class doesn't necessarily need to be an inner class of the test class, but
     * it's a convenient way to keep it close to the test code and still be something we can give
     * to an annotation.
     */
    static class MySubsystem implements EngineSubsystem {

        @Override
        public void initialise(GameEngine engine, Context rootContext) {
            var config = rootContext.getValue(PlayerConfig.class);
            config.playerName.set(PLAYER_NAME);
        }

        @Override
        public String getName() {
            // TODO: provide default implementation of EngineSubsystem.getName.
            //     The interface requires we implement this method, but test-only subsystems aren't
            //     player-visible.
            return this.getClass().getSimpleName();
        }
    }
}
