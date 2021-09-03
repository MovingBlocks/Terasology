// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.items


import org.terasology.cli.config.Config

class MetaItem extends Item implements GitItem<MetaItem>{
    MetaItem(String name) {
        super(name, new File(Config.META.directory,name))
    }
}
