// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.items

import org.terasology.cli.commands.BaseCommandType
import org.terasology.cli.config.ItemConfig
import org.terasology.cli.options.GitOptions

abstract class ItemCommand<T> extends BaseCommandType implements GitOptions {
    final ItemConfig config

    ItemCommand(ItemConfig config) {
        this.config = config
    }

    abstract T create(String name)

    List<T> listLocal() {
        List<T> result = []
        config.directory.eachDir({ dir ->
            def name = dir.getName()
            if (!(name in config.excludes)) {
                result << create(name)
            }
        })
        return result
    }

}
