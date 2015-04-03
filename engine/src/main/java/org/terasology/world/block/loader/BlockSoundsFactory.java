/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.world.block.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.audio.StaticSound;
import org.terasology.world.block.BlockSounds;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates block sounds objects from data driven block sounds definitions.
 * <br><br>
 * Responsible for resolving the actual sound assets referenced by the block sounds definition.
 */
public class BlockSoundsFactory {

    private static final Logger logger = LoggerFactory.getLogger(BlockSoundsFactory.class);

    public BlockSounds create(AssetUri assetUri, BlockSoundsDefinition soundsDefinition) {

        String uri = assetUri.toSimpleString();

        return new BlockSounds(
            uri,
            resolveSounds(uri, soundsDefinition.getStepSounds()),
            resolveSounds(uri, soundsDefinition.getDestroySounds()),
            resolveSounds(uri, soundsDefinition.getDigSounds())
        );

    }

    private static List<StaticSound> resolveSounds(String sourceUri, List<String> soundAssets) {

        List<StaticSound> result = new ArrayList<>(soundAssets.size());

        for (String soundAsset : soundAssets) {
            StaticSound sound = Assets.getSound(soundAsset);
            if (sound == null) {
                logger.warn("Unable to find sound {} referenced by block sounds {}.",
                    soundAsset, sourceUri);
            } else {
                result.add(sound);
            }
        }

        return result;

    }

}
