// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.managers

/**
 * Marks a managed item as having the capacity to have dependencies, forcing implementation of a way to parse them.
 */
interface DependencyProvider {
    List<String> parseDependencies(File targetDirectory, String itemToCheck)
}
