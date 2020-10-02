// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.event;

import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.terasology.entitySystem.event.Event;

public class Vector3fConstant implements Event {
    public Vector3fc v1;
    public Vector4fc v2;
    public Vector2fc v3;
}
