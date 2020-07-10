// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli
/**
 * Simple command type class that indicates the command deals with "items" - nested Git roots representing application elements
 */
abstract class ItemCommand extends BaseCommand {
    /**
     * Return a manager class for interacting with the specific type of item
     * @param optionGitOrigin if the user indicated an alternative Git origin it will be used to vary some URLs
     * @return the instantiated item type manager class
     */
    abstract ManagedItem getManager(String optionGitOrigin)
}
