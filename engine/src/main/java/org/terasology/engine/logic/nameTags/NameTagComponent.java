// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.nameTags;

import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.context.annotation.API;
import org.terasology.nui.Color;

/**
 * Will make the entity have a name tag overhead in the 3D view.
 *
 * The text on name tag is based on the {@link DisplayNameComponent} this entity.
 *
 * The color of the name tag is based on the {@link org.terasology.engine.network.ColorComponent} of this entity
 */
@API
public class NameTagComponent implements Component<NameTagComponent> {

    @Replicate
    public float yOffset = 0.3f;

    @Replicate
    public String text;

    @Replicate
    public Color textColor = Color.WHITE;

    @Replicate
    public float scale = 1f;

    @Override
    public void copyFrom(NameTagComponent other) {
        this.yOffset = other.yOffset;
        this.text = other.text;
        this.textColor = new Color(other.textColor);
        this.scale = other.scale;
    }
}
