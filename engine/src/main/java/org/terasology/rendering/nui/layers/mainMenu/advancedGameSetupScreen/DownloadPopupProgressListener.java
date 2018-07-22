/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.mainMenu.advancedGameSetupScreen;

import org.terasology.rendering.nui.layers.mainMenu.WaitPopup;
import org.terasology.utilities.download.MultiFileTransferProgressListener;

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
