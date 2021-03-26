// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.structure;

import org.terasology.engine.entitySystem.Component;

/**
 * Component for block entities that wish to describe their structural dependency on other blocks. One can describe
 * what are its support sides. If a block describes multiple sides as "allowed", at least one of them is required to
 * exist in order for the block to be "structurally sound".
 *
 * As an example - a chandelier would have topAllowed=true, most of building blocks for houses would be -
 * bottomAllowed=true, sideAllowed=true, table (furniture) would be bottomAllowed=true.
 *
 */
public class SideBlockSupportRequiredComponent implements Component {
    public boolean topAllowed;
    public boolean sideAllowed;
    public boolean bottomAllowed;
    public long dropDelay;
}
