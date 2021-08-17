// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.util

import groovy.json.JsonSlurper

import java.nio.charset.StandardCharsets

@Singleton
class ModuleIndex {
    JsonSlurper slurper = new JsonSlurper()
    Object[] cache;

    Object[] getData(){
        // TODO: implement ETAG at meta.terasology.org and use it there
        if(cache == null) {
            if(Constants.ModuleCacheFile.exists()){
                cache = slurper.parse(Constants.ModuleCacheFile,"UTF8")
            } else {

                URL modules = new URL(Constants.ModuleIndexUrl)

                String content = modules.text
                Constants.ModuleCacheFile.write(content, "UTF8")
                cache = slurper.parse(content.toCharArray())
            }
        }
        return cache;
    }

}
