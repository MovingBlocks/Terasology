package org.terasology.model.blocks;

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

//import ...

/**
 * A more specific type of Block, could put some growth logic here or just hold related stats
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class PlantBlock extends Block {

    /**
     * Determines whether or not this plant requires sunlight for whatever purpose
     */
    protected boolean _requiresSunlight;

    /**
     * Constructor that first inherits defaults from Block and then sets its own
     */
    public PlantBlock() {
        // Inherit defaults
        super();

        // Override some Block defaults with PlantBlock-level defaults
        withTitle("Untitled plant block");
        withAllowBlockAttachment(false);      // Generally can't attach stuff to plants!

        // Define PlantBlock-unique defaults
        _requiresSunlight = true;           // Most plants like sunlight (but not necessarily all!)
        //_blockCondition = "Dirt"          // Need to flesh this one out more, how are we storing it?

    }

}