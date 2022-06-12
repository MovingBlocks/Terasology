// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.Test;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;

import static com.google.common.truth.Truth.assertThat;

@IntegrationEnvironment(networkMode = NetworkMode.DEDICATED_SERVER)
public class NetworkModeLocalServerTest {
    @Test
    void testNetworkMode(NetworkSystem network) {
        assertThat(network.getMode()).isEqualTo(NetworkMode.DEDICATED_SERVER);
    }

    @Test
    void testForLocalPlayer(NetworkSystem network, LocalPlayer player) {
        assertThat(network.getPlayers()).hasSize(1);
        var client = network.getPlayers().iterator().next();
        assertThat(client.isLocal()).isTrue();
        assertThat(player.isValid()).isTrue();
    }
}
