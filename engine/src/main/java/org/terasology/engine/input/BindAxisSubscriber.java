// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input;

import org.terasology.engine.entitySystem.entity.EntityRef;

@FunctionalInterface
public interface BindAxisSubscriber {
    void update(float value, float delta, EntityRef target);
}
