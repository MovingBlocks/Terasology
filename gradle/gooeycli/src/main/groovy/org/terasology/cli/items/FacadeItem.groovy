// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.items

import org.terasology.cli.config.Config
import org.terasology.cli.config.GradleAwareConfig

class FacadeItem extends Item implements GitItem<FacadeItem>, GradleItem<GradleAwareConfig> {
    FacadeItem(String name) {
        super(name, new File(Config.FACADE.directory, name))
    }

    @Override
    GradleAwareConfig getConfig() {
        return Config.FACADE
    }
}
