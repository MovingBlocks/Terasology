// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import com.google.common.collect.Lists;
import org.terasology.engine.rendering.assets.animation.MeshAnimation;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;

/**
 * The component gets currently only used to define the idle stand animations.
 *
 */
public class StandComponent implements Component<StandComponent> {
    /**
     * A pool of idle stand animations. It gets currently only used by behavior trees to make a skeletal mesh perform a
     * idle stand animation loop. The animations of the pool will be picked by random. The result is a randomized
     * animation loop. The same animation can be put multiple times in the pool, so that it will be chosen more
     * frequently.
     */
    public List<MeshAnimation> animationPool = Lists.newArrayList();

    @Override
    public void copy(StandComponent other) {
        this.animationPool = Lists.newArrayList(other.animationPool);
    }
}
