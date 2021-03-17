// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players;

import org.joml.Vector3f;
import org.terasology.engine.rendering.logic.VisualComponent;

public class RemotePersonHeldItemTransformComponent implements VisualComponent {
    public Vector3f rotateDegrees = new Vector3f();
    public Vector3f translate = new Vector3f();
    public float scale = 1f;
}
