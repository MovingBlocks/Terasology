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

package org.terasology.network;

import java.util.List;
import java.util.Map;

import org.terasology.module.ModuleMetadata;
import org.terasology.naming.NameVersion;
import org.terasology.world.internal.WorldInfo;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public interface ServerInfoMessage {

    /**
     * @return
     */
    String getGameName();

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

}
