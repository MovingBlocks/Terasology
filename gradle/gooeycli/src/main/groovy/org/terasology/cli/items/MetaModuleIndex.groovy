// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.items

import groovy.json.JsonSlurper
import org.terasology.cli.config.Config

//TODO deps handling
trait MetaModuleIndex implements RemoteIndex<ModuleItem> {

    JsonSlurper slurper = new JsonSlurper()
    Object[] cache

    List<ModuleItem> listRemote() {
        if( cache == null) {
            URL modules = new URL(Config.MetaModuleIndexUrl)
            String content = modules.text
            cache = slurper.parse(content.toCharArray())
        }

        return cache*.id.collect() { new ModuleItem(it as String) }
    }
}