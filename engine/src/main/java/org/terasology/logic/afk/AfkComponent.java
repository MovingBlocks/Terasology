// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.logic.afk;

import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;

public class AfkComponent implements Component {

    @Replicate
    public boolean afk;

}
