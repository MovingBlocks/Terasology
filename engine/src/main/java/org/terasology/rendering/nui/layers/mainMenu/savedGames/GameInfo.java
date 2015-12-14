/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.mainMenu.savedGames;

import org.terasology.game.GameManifest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 */
public class GameInfo {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private Date timestamp;
    private GameManifest manifest;

    public GameInfo(GameManifest manifest, Date timestamp) {
        this.manifest = manifest;
        this.timestamp = timestamp;
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
}
