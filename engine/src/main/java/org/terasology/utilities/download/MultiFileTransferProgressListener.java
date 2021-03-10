// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.download;

public interface MultiFileTransferProgressListener {

    void onSizeMetadataProgress(int index, int totalUrls);

    void onDownloadProgress(long totalTransferredBytes, long totalBytes, int completedFiles, int nFiles);
}
