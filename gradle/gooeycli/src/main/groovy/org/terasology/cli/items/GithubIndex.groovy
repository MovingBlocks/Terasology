// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.items

import groovy.json.JsonSlurper

trait GithubIndex<T> implements RemoteIndex<T> {

    abstract String resolveOrigin()

    JsonSlurper slurper = new JsonSlurper()
    Object[] cache

    List<ModuleItem> listRemote() {
        String origin = resolveOrigin()

        if (cache == null) {
            URL modules = new URL("https://api.github.com/users/$origin/repos?per_page=999")
            String content = modules.text
            cache = slurper.parse(content.toCharArray())
        }
        return cache[0].collect { new ModuleItem(it.name as String) }
    }
}