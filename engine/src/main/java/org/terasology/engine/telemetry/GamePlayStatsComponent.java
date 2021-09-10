// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.telemetry;

import com.google.common.collect.Maps;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * A component stocks game play stats such as blocks destroyed, blocks placed, etc.
 */
public class GamePlayStatsComponent implements Component<GamePlayStatsComponent> {

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

    @Override
    public void copyFrom(GamePlayStatsComponent other) {
        this.blockDestroyedMap = Maps.newHashMap(other.blockDestroyedMap);
        this.blockPlacedMap = Maps.newHashMap(other.blockPlacedMap);
        this.distanceTraveled = other.distanceTraveled;
        this.playTimeMinute = other.playTimeMinute;
        this.creatureKilled = Maps.newHashMap(other.creatureKilled);
    }
}
