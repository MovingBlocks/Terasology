// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.nameTags;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.nui.Color;

/**
 * Will make the entity have a name tag overhead in the 3D view.
 * <p>
 * The text on name tag is based on the {@link org.terasology.logic.common.DisplayNameComponent} this entity.
 * <p>
 * The color of the name tag is based on the {@link org.terasology.network.ColorComponent} of this entity
 */
@API
public class NameTagComponent implements Component {

    @Replicate
    public float yOffset = 0.3f;

    @Replicate
    public String text;

    @Replicate
    public Color textColor = Color.WHITE;

    @Replicate
    public float scale = 1f;
}
