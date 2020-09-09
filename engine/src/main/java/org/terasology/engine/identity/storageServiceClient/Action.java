// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.identity.storageServiceClient;

/**
 * Represents an interaction that can be made with the storage service server.
 */
interface Action {

    void perform(StorageServiceWorker worker);
}
