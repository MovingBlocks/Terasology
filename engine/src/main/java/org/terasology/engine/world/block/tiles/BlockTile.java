// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.tiles;

import com.google.common.collect.Lists;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.context.annotation.API;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@API
public class BlockTile extends Asset<TileData> {
    private BufferedImage[] images;
    private boolean autoBlock;
    private List<Consumer<BlockTile>> reloadListeners = Collections.synchronizedList(Lists.newArrayList());

    public BlockTile(ResourceUrn urn, AssetType<?, TileData> assetType, TileData data) {
        super(urn, assetType);
        reload(data);
    }

    public BufferedImage getImage() {
        return getImage(0);
    }

    public BufferedImage getImage(int i) {
        return images[i];
    }

    public int getLength() {
        return images.length;
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
        this.images = tileData.getImages();
        this.autoBlock = tileData.isAutoBlock();
        for (Consumer<BlockTile> listener : reloadListeners) {
            listener.accept(this);
        }
    }

}
