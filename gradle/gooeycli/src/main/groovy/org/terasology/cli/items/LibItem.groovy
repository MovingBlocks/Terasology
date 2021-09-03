// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.items


import org.terasology.cli.config.Config

class LibItem extends Item implements GitItem<LibItem> {
    LibItem(String name) {
        super(name, new File(Config.LIB.directory,name))
    }
}
