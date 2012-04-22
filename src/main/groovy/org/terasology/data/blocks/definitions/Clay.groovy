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
 * Clay is not a single mineral, but a number of minerals. When most clays are
 * wet, they become "plastic" meaning they can be formed and molded into shapes.
 * When they are "fired" (exposed to very high temperatures), the water is driven
 * off and they become as hard as stone.
 */
block {
    version = 1
    shape = "cube"

    hardness = 16

    physics {
        mass = 128000
    }
}