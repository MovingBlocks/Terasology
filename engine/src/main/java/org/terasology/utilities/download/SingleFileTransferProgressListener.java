// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.download;

@FunctionalInterface
public interface SingleFileTransferProgressListener {

    void onProgress(long transferredBytes, long totalBytes);
}
