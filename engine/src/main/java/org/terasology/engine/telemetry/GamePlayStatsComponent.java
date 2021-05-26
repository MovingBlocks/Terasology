// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.telemetry;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

import java.util.HashMap;
import java.util.Map;

/**
 * A component stocks game play stats such as blocks destroyed, blocks placed, etc.
 */
public class GamePlayStatsComponent implements Component {

    @Replicate
    public Map<String, Integer> blockDestroyedMap = new HashMap<>();

    @Replicate
    public Map<String, Integer> blockPlacedMap = new HashMap<>();

    @Replicate
    public float distanceTraveled;

    @Replicate
    public float playTimeMinute;

    @Replicate
    public Map<String, Integer> creatureKilled = new HashMap<>();
}
