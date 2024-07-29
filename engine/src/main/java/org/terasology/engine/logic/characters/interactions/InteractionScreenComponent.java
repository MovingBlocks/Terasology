// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters.interactions;

import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.context.annotation.API;

/**
 * Entities with this component will show an UI during interactions.
 *
 */
@API
public class InteractionScreenComponent implements Component<InteractionScreenComponent> {
    public String screen;

    @Override
    public void copyFrom(InteractionScreenComponent other) {
        this.screen = other.screen;
    }
}
