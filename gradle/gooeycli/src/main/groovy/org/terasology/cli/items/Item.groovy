// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.items

class Item {

    final String name
    final File dir

    Item(String name, File dir) {
        this.name = name
        this.dir = dir
    }
}