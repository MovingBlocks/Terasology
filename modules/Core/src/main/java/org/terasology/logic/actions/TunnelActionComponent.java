/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.actions;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.health.EngineDamageTypes;

/**
 */
public class TunnelActionComponent implements Component {
    /**
     * The most blocks that can be destroyed before the action ends (counts duplicates, so actually way lower)
     */
    public int maxDestroyedBlocks = 5000;

    /**
     * How many effects to display at the most
     */
    public int maxParticalEffects = 4;

    /**
     * The max number of "steps" we'll take along the direction of the tunnel to pick explosive points
     */
    public int maxTunnelDepth = 64;

    /**
     * The max number of rays to cast at each chosen spot in the path of the tunnel to hit target blocks
     */
    public int maxRaysCast = 512;

    public int damageAmount = 1000;

    public Prefab damageType = EngineDamageTypes.EXPLOSIVE.get();

    /**
     * The amount of block positions that should be skipped from selection
     */
    public float thoroughness = 0.25f;

    public float explosiveForce = 200f;


}
