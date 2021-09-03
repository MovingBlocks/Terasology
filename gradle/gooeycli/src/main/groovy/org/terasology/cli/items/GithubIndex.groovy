// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.items

import groovy.json.JsonSlurper

trait GithubIndex<T> implements RemoteIndex<T> {

    abstract String resolveOrigin()

    JsonSlurper slurper = new JsonSlurper()
    List<Object> cache

    List<ModuleItem> listRemote() {
        String origin = resolveOrigin()

        if (cache == null) {
            cache = []
            int i = 1
            boolean keepGoing = true // Groovy haven't  do ... while T_T
            while (keepGoing) {
                URL modules = new URL("https://api.github.com/users/$origin/repos?per_page=999?page=$i")
                String content = modules.text
                List<Object> parsed = slurper.parse(content.toCharArray())
                if(parsed.size() == 0) {
                    keepGoing = false
                }
                cache << parsed
                i++
            }

        }


        return cache.collect { new ModuleItem(it.name as String) }
    }
}