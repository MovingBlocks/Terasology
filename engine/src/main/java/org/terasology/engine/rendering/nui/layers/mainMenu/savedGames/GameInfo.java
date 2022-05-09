// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.savedGames;

import org.terasology.engine.game.GameManifest;

import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Contains information about saved game.
 */
public class GameInfo {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private Date timestamp;
    private final GameManifest manifest;
    private Path savePath;

    public GameInfo(GameManifest manifest, Date timestamp, Path savePath) {
        this.manifest = checkNotNull(manifest, "manifest");
        this.timestamp = timestamp;
        this.savePath = savePath;
    }

    @Override
    public String toString() {
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);
        return manifest.getTitle() + "\n" + format.format(timestamp);
    }

    public Date getTimestamp() {
        return new Date(timestamp.getTime());
    }

    public GameManifest getManifest() {
        return manifest;
    }

    public Path getSavePath() {
        return savePath;
    }
}
