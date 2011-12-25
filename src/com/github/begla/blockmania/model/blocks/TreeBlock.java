package com.github.begla.blockmania.model.blocks;

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
 * A Tree is a more specific kind of Plant. Mainly it always has leaves.
 * This doesn't really matter to tree blocks, but _tree objects_ might have some use for it...
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class TreeBlock extends Block {

    /**
     * Hmm, what's unique about trees here?
     */
    protected boolean _whut;

    /**
     * Constructor that first inherits defaults from Block and then sets its own
     */
    public TreeBlock() {
        // Inherit defaults
        super();

        // Override some defaults
        _title = "Untitled tree block";
        _allowBlockAttachment = true;      // Trees, unlike plain plants, _can_ in fact have stuff attached

        // Define plant-specific defaults
        _whut = true;           // Will probably come up with something special

    }

}