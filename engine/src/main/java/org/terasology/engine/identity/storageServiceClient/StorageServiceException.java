// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.identity.storageServiceClient;

public class StorageServiceException extends Exception {

    StorageServiceException(String message) {
        super("The server answered with this error message: " + message);
    }

    StorageServiceException() {
        super("The server answer could not be parsed");
    }
}
