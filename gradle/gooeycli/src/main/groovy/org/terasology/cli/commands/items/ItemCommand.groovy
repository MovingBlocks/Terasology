// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.items

import org.terasology.cli.commands.BaseCommandType
import org.terasology.cli.config.ItemConfig
import org.terasology.cli.items.Item
import org.terasology.cli.items.RemoteIndex
import org.terasology.cli.options.GitOptions
import picocli.CommandLine

abstract class ItemCommand<T> extends BaseCommandType implements GitOptions, RemoteIndex {
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

    List<String> getAbsentItemsInRemote(List<String> items) {
        List<Item> remoteList = listRemote()
        if(remoteList == null) {
            return []
        }
        return items.findAll {!(it in remoteList*.name)}
    }

    boolean validItems(List<String> items){
        List<String> unknownItems = getAbsentItemsInRemote(items)
        if(unknownItems.empty){
            return true
        } else {
            println CommandLine.Help.Ansi.AUTO.string("@|red Unknown Items: ${unknownItems}|@")
            return false
        }
    }
}
