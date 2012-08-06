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
 * A blocky liquid? Logic error! But deliciously hierarchical!
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class LiquidBlock extends Block {

    /**
     * How "thick" is this liquid. Lower is thinner
     */
    protected int _viscosity;

    /**
     * Constructor that first inherits defaults from Block and then sets its own
     */
    public LiquidBlock() {
        // Inherit defaults
        super();

        // Override some defaults
        withTitle("Untitled liquid block");

        withAllowBlockAttachment(false);      // Yeah, no attaching stuff to liquids much...
        withLiquid(true);                     // Liquids are indeed liquids
        withTranslucent(true);
        withBypassSelectionRay(true);
        withBlockForm(BLOCK_FORM.LOWERED_BLOCK);
        withPenetrable(true);
        withCastsShadows(true);

        // Define liquid-specific defaults
        _viscosity = 50;           // No clue how viscous "50" would be or if there's a point in having a default...
    }

}