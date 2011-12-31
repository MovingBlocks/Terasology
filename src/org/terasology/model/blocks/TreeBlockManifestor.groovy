package org.terasology.model.blocks

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

//import ...

/**
 * BlockManifestor for TreeBlocks, a specific flavor of PlantBlock
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
class TreeBlockManifestor extends BlockManifestor {

    /**
     * The Block class this Manifestor loads & prepares (so BlockManifestor.loadBlockDefinitions can be generic)
     * @return the instantiated and prepared class
     */
    private instantiateBlock(ConfigObject blockConfig) {
        // Construct the class - this loads the Block-level defaults
        TreeBlock b = new TreeBlock()

        // Now apply Block-level details from Groovy (which may overwrite constructor defaults)
        prepareBlock(b, blockConfig)

        // Return the prepared class
        return b
    }

    /**
     * Adds TreeBlock-specific values to this Block. First calls super() to load parent properties
     * @param b The TreeBlock we're preparing (guaranteed to be a TreeBlock or a child there-of)
     * @param blockConfig The ConfigSlurper-produced props from the Groovy definition
     * @return The finished TreeBlock object we'll store in the BlockManager (returned via reference)
     */
    protected prepareBlock(TreeBlock b, ConfigObject blockConfig) {
        // Load Block-level Groovy details first (constructors handle defaults)
        super.prepareBlock(b, blockConfig)

        // Now load TreeBlock Groovy details, which may overwrite parent Groovy details just loaded :-)

    }
}
