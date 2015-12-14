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
package org.terasology.world.block.tiles;

import com.google.common.collect.Lists;
import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 */
public class BlockTile extends Asset<TileData> {
    private BufferedImage image;
    private boolean autoBlock;
    private List<Consumer<BlockTile>> reloadListeners = Collections.synchronizedList(Lists.newArrayList());

    public BlockTile(ResourceUrn urn, AssetType<?, TileData> assetType, TileData data) {
        super(urn, assetType);
        reload(data);
    }

    public BufferedImage getImage() {
        return image;
    }

    public boolean isAutoBlock() {
        return autoBlock;
    }

    public synchronized void subscribe(Consumer<BlockTile> reloadListener) {
        this.reloadListeners.add(reloadListener);
    }

    public synchronized void unsubscribe(Consumer<BlockTile> reloadListener) {
        this.reloadListeners.remove(reloadListener);
    }

    @Override
    protected void doReload(TileData tileData) {
        this.image = tileData.getImage();
        this.autoBlock = tileData.isAutoBlock();
        for (Consumer<BlockTile> listener : reloadListeners) {
            listener.accept(this);
        }
    }

}
