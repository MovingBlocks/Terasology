package org.terasology.model.blocks

import org.terasology.utilities.ClasspathResourceLoader

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
 * BlockManifestor for PlantBlocks
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
class PlantBlockManifestor extends BlockManifestor {

    public PlantBlockManifestor(ClasspathResourceLoader loader) {
        _resourceLoader = loader;
        fixSavePaths()
    }

    /**
     * The Block class this Manifestor loads & prepares (so BlockManifestor.loadBlockDefinitions can be generic)
     * @return the instantiated and prepared class
     */
    private instantiateBlock(ConfigObject blockConfig) {
        // Construct the class - this loads the Block-level defaults
        PlantBlock b = new PlantBlock()

        // Now apply Block-level details from Groovy (which may overwrite constructor defaults)
        prepareBlock(b, blockConfig)

        // Return the prepared class
        return b
    }

    /**
     * Adds PlantBlock-specific values to this Block. First calls super() to load basic Block properties
     * @param b The PlantBlock we're preparing (guaranteed to be a PlantBlock or a child there-of)
     * @param blockConfig The ConfigSlurper-produced props from the Groovy definition
     * @return The finished PlantBlock object we'll store in the BlockManager (returned via reference)
     */
    protected prepareBlock(PlantBlock b, ConfigObject blockConfig) {
        // Load Block-level Groovy details first (the Block and PlantBlock constructors handle defaults)
        super.prepareBlock(b, blockConfig)

        // Now load PlantBlock Groovy details, which may overwrite Block-level Groovy details just loaded :-)

    }
}
