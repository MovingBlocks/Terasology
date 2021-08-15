// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.afk;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

public class AfkComponent implements Component {

    @Replicate
    public boolean afk;

}
