// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.logic;

import org.terasology.engine.rendering.assets.animation.MeshAnimation;
import org.terasology.gestalt.entitysystem.event.Event;

public class AnimEndEvent implements Event {
    private MeshAnimation animation;

    public AnimEndEvent(MeshAnimation animation) {
        this.animation = animation;
    }

    public MeshAnimation getAnimation() {
        return animation;
    }
}
