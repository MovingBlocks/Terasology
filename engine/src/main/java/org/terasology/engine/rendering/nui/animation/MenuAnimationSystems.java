// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.animation;

import org.terasology.engine.config.Config;
import org.terasology.engine.config.RenderingConfig;
import org.terasology.engine.registry.CoreRegistry;

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
        MenuAnimationSystem swipe = new SwipeMenuAnimationSystem(0.25f, SwipeMenuAnimationSystem.Direction.LEFT_TO_RIGHT);
        MenuAnimationSystem instant = new MenuAnimationSystemStub();
        Supplier<MenuAnimationSystem> provider = () -> config.isAnimatedMenu() ? swipe : instant;
        return new DeferredMenuAnimationSystem(provider);
    }
}
