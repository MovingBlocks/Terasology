// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.common;

import org.terasology.engine.entitySystem.Component;

/**
 * Indicate that an entity has a <i>range</i>.
 * <p>
 * This can be used to give items an extended range for activation, but may also be used to indicate any other ranged ability of an entity.
 */
public class RangeComponent implements Component {
    /**
     * The range measured in in-game blocks.
     */
    public float value;
}
