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

package org.terasology.network.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.module.ModuleInfo;
import org.terasology.engine.module.Version;
import org.terasology.network.ServerInfoMessage;
import org.terasology.protobuf.NetData;
import org.terasology.world.internal.WorldInfo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Default implementation of {@link ServerInfoMessage}.
 * Wraps the Protocol Buffer implementation
 * @author Martin Steiger
 */
class ServerInfoMessageImpl implements ServerInfoMessage {

    private static final Logger logger = LoggerFactory.getLogger(ServerInfoMessageImpl.class);
    
    private final NetData.ServerInfoMessage info;

    ServerInfoMessageImpl(NetData.ServerInfoMessage pbInfo) {
        this.info = pbInfo;
        this.info.getBlockIdList();
    }
    
    @Override
    public String getGameName() {
        return info.getGameName();
    }

    @Override
    public List<WorldInfo> getWorldInfoList() {
        List<WorldInfo> result = Lists.newArrayList();
        
        for (NetData.WorldInfo pbWorldInfo : info.getWorldInfoList()) {
            WorldInfo worldInfo = new WorldInfo();
            worldInfo.setTime(pbWorldInfo.getTime());
            worldInfo.setTitle(pbWorldInfo.getTitle());
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
    public List<ModuleInfo> getModuleList() {
        List<ModuleInfo> result = Lists.newArrayList();
        
        for (NetData.ModuleInfo moduleInfo : info.getModuleList()) {
            if (!moduleInfo.hasModuleId() || !moduleInfo.hasModuleVersion() || Version.create(moduleInfo.getModuleVersion()) == null) {
                logger.error("Received incomplete module info");
            } else {
                ModuleInfo mi = new ModuleInfo();
                mi.setId(moduleInfo.getModuleId());
                mi.setVersion(moduleInfo.getModuleVersion());
                // other info is not communicated
                result.add(mi);
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
