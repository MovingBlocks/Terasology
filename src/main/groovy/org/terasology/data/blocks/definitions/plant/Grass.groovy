/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.data.blocks.definitions.plant

import org.terasology.model.blocks.Block.COLOR_SOURCE
import org.terasology.math.Side

/**
 * Grass is a little more interesting as its faces differ.
 * Note how we don't need to indicate the top face (stays default)
 * If "faces" wasn't present Block would simply assume all the faces are the same
 * and that the appropriate image file to use equals the Block name + .png
 */
block {
    // Serialization (if it ever makes sense)
    version = 1
    shape = "Cube"
    // Grass is actually a solid grass-covered block, so it is OK to attach stuff to it
    allowBlockAttachment = true

    // Graphics
    faces {                     // "faces" being present means we'll override some with a different graphic
        sides = "GrassSide"     // sides means all four (not top/bottom) and can itself be overridden
        bottom = "Dirt"         // Since we add all images to the same map we don't need to worry about dir levels
    }

    // Works as an Enum :-)
    colorsource = COLOR_SOURCE.COLOR_LUT
    affectedByLut = [(Side.BOTTOM):(false)]

    // Plant stuff! Helps guide growth and if "evolve" is present a maxed plant may transform type
    // Grass grows from 0 (dirt/non-existent) to 1 (grass block), may rarely grow again and evolve
    plant {
        growth {
            //blocks = "Dirt"     // Where can it possibly grow - this is default so not needed
            factor = 0.25       // How frequent / likely growth is
            weight = -0.95      // Growth factor impact with sustained growth (likelihood to continue)
            increment = 1       // How much growth when growing (if this ever makes sense)
            max = 1             // Cap for maximum maturity (before evolving, if possible)

            // Available options to evolve into
            evolve {
                targets = ["TallGrass", "YellowFlower", "RedFlower"]    // Possible evolutions
                weights = [85, 10, 5]                                    // Likelihood of each
            }
        }
    }
}