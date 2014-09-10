/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.engine.modes.loadProcesses;

import com.google.common.collect.Lists;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;

import java.util.Iterator;

/**
 * @author Immortius
 */
public class CacheTextures extends StepBasedLoadProcess {
    private Iterator<AssetUri> uris;

    @Override
    public String getMessage() {
        return "Caching Textures...";
    }

    @Override
    public void begin() {
        uris = Assets.list(AssetType.TEXTURE).iterator();
        setTotalSteps(Lists.newArrayList(Assets.list(AssetType.TEXTURE)).size());
    }

    @Override
    public boolean step() {
        AssetUri textureURI = uris.next();
        Assets.get(textureURI);
        stepDone();
        return !uris.hasNext();
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
