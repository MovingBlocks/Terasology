// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import com.google.common.collect.Lists;

import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.entitySystem.Component;

import java.util.List;

public final class CharacterSoundComponent implements Component {

    public List<StaticSound> footstepSounds = Lists.newArrayList();
    public List<StaticSound> damageSounds = Lists.newArrayList();
    public List<StaticSound> landingSounds = Lists.newArrayList();
    public List<StaticSound> jumpSounds = Lists.newArrayList();
    public List<StaticSound> deathSounds = Lists.newArrayList();
    public List<StaticSound> respawnSounds = Lists.newArrayList();
    // TODO: put these on components on the liquid block prefabs instead (for per-liquid swim sounds)
    public List<StaticSound> swimSounds = Lists.newArrayList();
    public List<StaticSound> enterWaterSounds = Lists.newArrayList();
    public List<StaticSound> leaveWaterSounds = Lists.newArrayList();

    public float footstepVolume = 1.0f;
    public float damageVolume = 1.0f;
    public float jumpVolume = 1.0f;
    public float landingVolume = 1.0f;
    public float deathVolume = 1.0f;
    public float respawnVolume = 1.0f;
    public float swimmingVolume = 1.0f;
    public float diveVolume = 1.0f;

    public long lastSoundTime;

}
