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
 * Like dolomite and chalk, limestone is a flux stone which forms entire
 * sedimentary layers. In general, limestone layers tend to yield more
 * ores and gems per volume mined than those of other stones, which makes
 * them a prime candidate for exploratory mining.
 */
block {
    version = 1
    shape = "cube"

    hardness = 16

    physics {
        mass = 128000
    }
}