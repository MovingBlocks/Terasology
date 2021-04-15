// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.advancedGameSetupScreen;

import org.terasology.engine.rendering.nui.layers.mainMenu.WaitPopup;
import org.terasology.engine.utilities.download.MultiFileTransferProgressListener;

public final class DownloadPopupProgressListener implements MultiFileTransferProgressListener {

    private static final String POPUP_TITLE = "Downloading required modules, please wait...";
    private WaitPopup popup;

    public DownloadPopupProgressListener(WaitPopup popup) {
        this.popup = popup;
    }

    @Override
    public void onSizeMetadataProgress(int index, int totalUrls) {
        popup.setMessage(POPUP_TITLE, String.format("Retrieving file size information - %d of %d", index, totalUrls));
    }

    @Override
    public void onDownloadProgress(long totalTransferredBytes, long totalBytes, int completedFiles, int nFiles) {
        int globalPercentage = (int) (totalTransferredBytes * 100f / totalBytes);
        String message = String.format("Downloaded modules: %d of %d\nDownloaded bytes: %d of %d\nGlobal progress: %d%%",
                completedFiles, nFiles, totalTransferredBytes, totalBytes, globalPercentage);
        popup.setMessage(POPUP_TITLE, message);
    }
}
