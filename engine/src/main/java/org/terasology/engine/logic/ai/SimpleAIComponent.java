// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.ai;

import org.terasology.engine.entitySystem.Component;
import org.terasology.math.geom.Vector3f;

/**
 *
 */
public final class SimpleAIComponent implements Component {

    public long lastChangeOfDirectionAt;
    public Vector3f movementTarget = new Vector3f();
    public boolean followingPlayer;

}
