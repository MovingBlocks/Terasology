// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.network.ColorComponent;
import org.terasology.nui.FontColor;

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
