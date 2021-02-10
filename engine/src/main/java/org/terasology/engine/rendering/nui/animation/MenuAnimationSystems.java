// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.rendering.nui.animation;

import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.animation.SwipeMenuAnimationSystem.Direction;

import java.util.function.Supplier;

/**
 * Controls animations to and from different screens
 */
public final class MenuAnimationSystems {

    private MenuAnimationSystems() {
        // no instances
    }

    public static MenuAnimationSystem createDefaultSwipeAnimation() {
        RenderingConfig config = CoreRegistry.get(Config.class).getRendering();
        MenuAnimationSystem swipe = new SwipeMenuAnimationSystem(0.25f, Direction.LEFT_TO_RIGHT);
        MenuAnimationSystem instant = new MenuAnimationSystemStub();
        Supplier<MenuAnimationSystem> provider = () -> config.isAnimatedMenu() ? swipe : instant;
        return new DeferredMenuAnimationSystem(provider);
    }
}
