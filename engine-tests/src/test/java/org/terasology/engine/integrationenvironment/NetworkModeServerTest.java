// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.integrationenvironment.jupiter.MTEExtension;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;

import static com.google.common.truth.Truth.assertThat;

@Tag("MteTest")
@ExtendWith(MTEExtension.class)
@IntegrationEnvironment(networkMode = NetworkMode.LISTEN_SERVER)
public class NetworkModeServerTest {
    @Test
    void testNetworkMode(NetworkSystem network) {
        assertThat(network.getMode()).isEqualTo(NetworkMode.LISTEN_SERVER);
    }

    @Test
    void testServerHasNoPlayers(NetworkSystem network, LocalPlayer player) {
        assertThat(network.getPlayers()).isEmpty();
        assertThat(player.isValid()).isFalse();
    }
}
