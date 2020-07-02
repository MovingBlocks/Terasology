// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.splash.widgets;

import org.terasology.engine.splash.graphics.Renderer;

public interface Widget {
    void render(Renderer renderer);

    default void update(double dt) {};
}
