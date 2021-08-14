// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.afk;

import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

public class AfkComponent implements Component<AfkComponent> {

    @Replicate
    public boolean afk;

    @Override
    public void copyFrom(AfkComponent other) {
        this.afk = other.afk;
    }
}
