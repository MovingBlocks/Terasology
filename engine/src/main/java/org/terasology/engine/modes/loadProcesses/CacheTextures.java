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

import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.rendering.assets.texture.Texture;

import java.util.Iterator;
import java.util.Set;

/**
 */
public class CacheTextures extends StepBasedLoadProcess {
    private Iterator<ResourceUrn> urns;

    @Override
    public String getMessage() {
        return "Caching Textures...";
    }

    @Override
    public void begin() {
        Set<ResourceUrn> list = Assets.list(Texture.class);
        urns = list.iterator();
        setTotalSteps(list.size());
    }

    @Override
    public boolean step() {
        ResourceUrn textureUrn = urns.next();
        Assets.get(textureUrn, Texture.class);
        stepDone();
        return !urns.hasNext();
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
