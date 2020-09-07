/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
        }) package org.terasology.input.binds.movement;

import org.terasology.gestalt.module.sandbox.API;
import org.terasology.input.InputCategory;
