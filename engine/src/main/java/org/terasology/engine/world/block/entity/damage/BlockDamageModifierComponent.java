// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.entity.damage;

import com.google.common.collect.Maps;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Map;

public class BlockDamageModifierComponent implements Component<BlockDamageModifierComponent> {

    public Map<String, Integer> materialDamageMultiplier = Maps.newHashMap();
    public float blockAnnihilationChance;
    public boolean skipPerBlockEffects;
    public boolean directPickup;
    public float impulsePower;

    @Override
    public void copy(BlockDamageModifierComponent other) {
        this.materialDamageMultiplier = other.materialDamageMultiplier;
        this.blockAnnihilationChance = other.blockAnnihilationChance;
        this.skipPerBlockEffects = other.skipPerBlockEffects;
        this.directPickup = other.directPickup;
        this.impulsePower = other.impulsePower;
    }
}
