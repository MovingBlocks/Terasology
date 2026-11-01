// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0


@API
@InputCategory(id = "movement",
        displayName = "${engine:menu#category-movement}",
        ordering = {
                "engine:forwards",
                "engine:backwards",
                "engine:left",
                "engine:right",
                "engine:toggleSpeedPermanently",
                "engine:toggleSpeedTemporarily",
                "engine:autoMoveMode",
                "engine:crouchMode",
                "engine:jump",
                "engine:crouch"
        }) package org.terasology.engine.input.binds.movement;

import org.terasology.context.annotation.API;
import org.terasology.input.InputCategory;
