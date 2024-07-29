// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.nui;

import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.context.annotation.API;
import org.terasology.nui.Color;

/**
 * Defines a renderable node used to display behavior trees.
 *
 */
@API
public class BehaviorNodeComponent implements Component<BehaviorNodeComponent> {
    public static final BehaviorNodeComponent DEFAULT = new BehaviorNodeComponent();

    public String action;                       // the node(s) to create
    public String name;                         // name used internally. should be unique.
    public String displayName;                  // name displayed in ui
    public String category;                     // for palette
    public String shape = "diamond";            // diamond or rect
    public Color color = Color.GREY;
    public Color textColor = Color.BLACK;
    public String description = "";

    @Override
    public String toString() {
        return displayName;
    }

    @Override
    public void copyFrom(BehaviorNodeComponent other) {
        this.action = other.action;
        this.name = other.name;
        this.displayName = other.displayName;
        this.category = other.category;
        this.shape = other.shape;
        this.color = new Color(other.color);
        this.textColor = new Color(other.textColor);
        this.description = other.description;
    }
}
