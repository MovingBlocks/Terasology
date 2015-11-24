/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.logic.players;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.network.ColorComponent;
import org.terasology.rendering.FontColor;

/**
 * Utility class. Currently only used as container for getColoredPlayerName.
 */
public final class PlayerUtil {

    private PlayerUtil() {
        // utility class
    }

    public static String getColoredPlayerName(EntityRef from) {
        DisplayNameComponent displayInfo = from.getComponent(DisplayNameComponent.class);
        ColorComponent colorInfo = from.getComponent(ColorComponent.class);
        String playerName = (displayInfo != null) ? displayInfo.name : "Unknown";

        if (colorInfo != null) {
            playerName = FontColor.getColored(playerName, colorInfo.color);
        }
        return playerName;
    }

}
