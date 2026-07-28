// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.subsystem.nakama;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NakamaConfigTest {
    @Test
    void autoConfigDefaultsAreDisabled() {
        NakamaAutoConfig config = new NakamaAutoConfig();
        assertFalse(config.enabled.get());
        assertEquals("bifrost.lobby", config.channel.get());
        assertEquals(7349, config.grpcPort.get());
        assertEquals(7350, config.wsPort.get());
        assertEquals("localhost", config.host.get());
        assertEquals("", config.playerName.get());
    }
}
