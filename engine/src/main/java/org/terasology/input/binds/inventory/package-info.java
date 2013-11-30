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
@InputCategory(id = "inventory",
        displayName = "Inventory",
        ordering = {
                "engine:useItem",
                "engine:dropItem",
                "engine:inventory",
                "engine:toolbarPrev",
                "engine:toolbarNext",
                "engine:toolbarSlot0",
                "engine:toolbarSlot1",
                "engine:toolbarSlot2",
                "engine:toolbarSlot3",
                "engine:toolbarSlot4",
                "engine:toolbarSlot5",
                "engine:toolbarSlot6",
                "engine:toolbarSlot7",
                "engine:toolbarSlot8",
                "engine:toolbarSlot9"
        }) package org.terasology.input.binds.inventory;

import org.terasology.engine.API;
import org.terasology.input.InputCategory;
