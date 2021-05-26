// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

import org.terasology.gestalt.naming.NameVersion;
import org.terasology.engine.world.internal.WorldInfo;

import java.util.List;
import java.util.Map;

/**
 * TODO Type description
 *
 */
public interface ServerInfoMessage {

    /**
     * @return
     */
    String getGameName();

    String getMOTD();

    List<WorldInfo> getWorldInfoList();

    /**
     * @return
     */
    List<String> getRegisterBlockFamilyList();

    /**
     * @return
     */
    Map<Integer, String> getBlockIds();

    /**
     * @return
     */
    long getTime();

    /**
     * @return
     */
    List<NameVersion> getModuleList();

    /**
     * @return the block height that is used to compute (water) reflections
     */
    float getReflectionHeight();

    /**
     * Get amount of online players from server
     */
    int getOnlinePlayersAmount();
}
