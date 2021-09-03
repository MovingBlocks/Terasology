// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.module

import groovy.json.JsonSlurper
import org.terasology.cli.config.Config

@Singleton
class ModuleIndex {
    JsonSlurper slurper = new JsonSlurper()
    Object[] cache

    Object[] getData() {
        // TODO: implement ETAG at meta.terasology.org and use it there
        if (cache == null) {
            if (Config.ModuleCacheFile.exists()
                    && Config.ModuleCacheFile.lastModified() + Config.ModuleCacheValidTime < System.currentTimeSeconds()) {
                cache = slurper.parse(Config.ModuleCacheFile, "UTF8")
            } else {

                URL modules = new URL(Config.MetaModuleIndexUrl)

                String content = modules.text
                Config.ModuleCacheFile.write(content, "UTF8")
                cache = slurper.parse(content.toCharArray())
            }
        }
        return cache
    }

}
