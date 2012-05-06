/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.data.blocks.definitions

/**
 * Phyllite is a metamorphic stone. These types of stone form as entire layers, containing some veins and pockets of
 * other minerals. Could be used in construction
 */
block {
    version = 1
    shape = "cube"

    faces {
        sides = "SnowSide"
        bottom = "Dirt"
    }
}