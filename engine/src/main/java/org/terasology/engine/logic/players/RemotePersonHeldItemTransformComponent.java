// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players;

import org.terasology.engine.rendering.logic.VisualComponent;
import org.terasology.math.geom.Vector3f;

public class RemotePersonHeldItemTransformComponent implements VisualComponent {
    public Vector3f rotateDegrees = Vector3f.zero();
    public Vector3f translate = Vector3f.zero();
    public float scale = 1f;
}
