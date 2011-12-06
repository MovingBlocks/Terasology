package com.github.begla.blockmania.data.blocks.plant

import com.github.begla.blockmania.blocks.Block.COLOR_SOURCE
import com.github.begla.blockmania.data.blocks.Dirt

/**
 * Grass is a little more interesting as its faces differ.
 * Note how we don't need to indicate the top face (stays default)
 * If "faces" wasn't present Block would simply assume all the faces are the same
 * and that the appropriate image file to use equals the Block name + .png
 */
block {
    // Serialization (if it ever makes sense)
    version = 1

    // Graphics
    faces {                     // "faces" being present means we'll override some with a different graphic
        // top = "Grass"        // This one is not needed as it is the default (image matching own name)
        bottom = "Dirt"         // NOTE: This image is actually at ../Dirt from the Plant texture dir...
        sides = "DirtGrass"     // sides means all four (not top/bottom) and can itself be overridden
        // left = DirtierGrass  // Example of how you could make one side differ without writing all 4
    }

    // This one might be more interesting - what will it return as, an enum?
    colorsource = COLOR_SOURCE.COLOR_LUT

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