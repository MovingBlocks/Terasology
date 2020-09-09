// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.block.internal;

import org.terasology.engine.world.block.family.BlockFamily;

@FunctionalInterface
public interface BlockRegistrationListener {

    void onBlockFamilyRegistered(BlockFamily family);
}
