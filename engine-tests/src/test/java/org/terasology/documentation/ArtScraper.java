/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.documentation;

import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.assets.ResourceUrn;
import org.terasology.world.block.tiles.BlockTile;

import java.util.Optional;
import java.util.Set;

public class ArtScraper extends TerasologyTestingEnvironment {

    @Test
    public void myTest() {
        Set<ResourceUrn> allTextures = assetManager.getAvailableAssets(BlockTile.class);

        //Set<ResourceUrn> allTextures = Assets.list(Texture.class);
        for (ResourceUrn texUrn : allTextures) {
            System.out.println(texUrn);
            //Optional<BlockTile> tile = assetManager.getAsset(texUrn, BlockTile.class);

        }
    }
}
