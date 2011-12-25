/**
 * This is a tree. Treat it well and it will grow tall and strong.
 * Idea is that all trees (as defined in the block/plant/tree package anyway  will have leaves
 * L-system generation stat sets could also go here (or in their own separate thing?)
 * Entirely separate trees would have their own tree + leaf block definitions (rename the plain "Tree" then)
 */
// TODO: Trees, wood, bark, planks, leaves, etc could use some more exact definition thoughts
block {
    // Serialization
    version = 1

    hardness = 6

    // Graphics
    faces {
        sides = "OakBark"
    }

    // Plant characteristics. Trees only world-gen atm so they "rarely grow" on dirt and spring instantly to full size
    plant {
        growth {
            //blocks = "Dirt"   // Where can it possibly grow - this is default so not needed
            factor = 0.05       // How frequent / likely growth is
            weight = -1         // Growth factor impact with sustained growth (likelihood to continue)
            increment = 50      // How much growth when growing (if this ever makes sense)
            max = 50            // Cap for maximum maturity (before evolving, if possible)

            //evolve - trees do not evolve, so don't need any stats
        }
    }

    // Tree characteristics
    tree {
        // Trees have leaves!
        leaves {
            block = "GreenLeaf"        // Reference to the block we use as leaves for this tree
            // Since color offsets make other colors, maybe any one tree can have different colors?
        }

        // We could probably put L-system generation stats here per tree type?
        generation {
            lstuff = "Goes here"
        }

        // Details on what kind of wood _processing_ this tree produces (not implemented yet)
        wood {
            block = "Plank"
        }
    }


}