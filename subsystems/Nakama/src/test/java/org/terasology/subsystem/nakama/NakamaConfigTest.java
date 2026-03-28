// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.subsystem.nakama;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NakamaConfigTest {
    @Test
    void defaultsAreDisabled() {
        NakamaConfig config = new NakamaConfig();
        assertFalse(config.isEnabled());
        assertEquals("bifrost.lobby", config.getChannel());
        assertEquals(7349, config.getGrpcPort());
        assertEquals(7350, config.getWsPort());
    }
}
