// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.Zones;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.world.zones.Zone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ZoneTest {

    private Zone zone;

    @BeforeEach
    public void setup() {
        zone = new Zone("Test", () -> true);
    }

    @Test
    public void testGetChildZones() {
        assertTrue(zone.getChildZones().isEmpty());
        Zone child = new Zone("Child", () -> false);
        zone.addZone(child);
        assertFalse(zone.getChildZones().isEmpty());
        assertTrue(zone.getChildZones().contains(child));
        assertThrows(Exception.class, () -> zone.getChildZone("Invalid name"));
        assertEquals(child, zone.getChildZone("Child"));
    }


}
