// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.naming.Name;
import org.terasology.gestalt.naming.NameVersion;
import org.terasology.engine.network.ServerInfoMessage;
import org.terasology.gestalt.naming.Version;
import org.terasology.protobuf.NetData;
import org.terasology.engine.world.internal.WorldInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link ServerInfoMessage}.
 * Wraps the Protocol Buffer implementation
 */
class ServerInfoMessageImpl implements ServerInfoMessage {

    private static final Logger logger = LoggerFactory.getLogger(ServerInfoMessageImpl.class);

    private final NetData.ServerInfoMessage info;

    ServerInfoMessageImpl(NetData.ServerInfoMessage pbInfo) {
        this.info = pbInfo;
    }

    @Override
    public String getGameName() {
        return info.getGameName();
    }

    @Override
    public String getMOTD() {
        return info.getMOTD();
    }

    @Override
    public List<WorldInfo> getWorldInfoList() {
        List<WorldInfo> result = Lists.newArrayList();

        for (NetData.WorldInfo pbWorldInfo : info.getWorldInfoList()) {
            WorldInfo worldInfo = new WorldInfo();
            worldInfo.setTime(pbWorldInfo.getTime());
            worldInfo.setTitle(pbWorldInfo.getTitle());
            result.add(worldInfo);
        }

        return result;
    }

    @Override
    public List<String> getRegisterBlockFamilyList() {
        return Collections.unmodifiableList(info.getRegisterBlockFamilyList());
    }

    @Override
    public long getTime() {
        return info.getTime();
    }

    @Override
    public float getReflectionHeight() {
        return info.getReflectionHeight();
    }

    @Override
    public int getOnlinePlayersAmount() {
        return info.getOnlinePlayersAmount();
    }

    @Override
    public List<NameVersion> getModuleList() {
        List<NameVersion> result = Lists.newArrayList();

        for (NetData.ModuleInfo moduleInfo : info.getModuleList()) {
            if (!moduleInfo.hasModuleId() || !moduleInfo.hasModuleVersion()) {
                logger.error("Received incomplete module info");
            } else {
                Name id = new Name(moduleInfo.getModuleId());
                Version version = new Version(moduleInfo.getModuleVersion());
                result.add(new NameVersion(id, version));
            }
        }

        return result;
    }

    @Override
    public Map<Integer, String> getBlockIds() {
        Map<Integer, String> result = Maps.newHashMap();

        for (int i = 0; i < info.getBlockIdCount(); ++i) {
            result.put(info.getBlockId(i), info.getBlockName(i));
        }

        return result;
    }
}
