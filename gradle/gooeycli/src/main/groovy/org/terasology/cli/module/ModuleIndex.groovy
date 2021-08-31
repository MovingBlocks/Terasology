// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.module

import groovy.json.JsonSlurper
import org.terasology.cli.util.Constants

@Singleton
class ModuleIndex {
    JsonSlurper slurper = new JsonSlurper()
    Object[] cache

    Object[] getData() {
        // TODO: implement ETAG at meta.terasology.org and use it there
        if (cache == null) {
            if (Constants.ModuleCacheFile.exists()
                    && Constants.ModuleCacheFile.lastModified() + Constants.ModuleCacheValidTime < System.currentTimeSeconds()) {
                cache = slurper.parse(Constants.ModuleCacheFile, "UTF8")
            } else {

                URL modules = new URL(Constants.ModuleIndexUrl)

                String content = modules.text
                Constants.ModuleCacheFile.write(content, "UTF8")
                cache = slurper.parse(content.toCharArray())
            }
        }
        return cache
    }

}
