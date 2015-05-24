/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.rendering.nui.layers.mainMenu.preview;

import java.nio.ByteBuffer;

import org.terasology.math.Rect2i;
import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.layers.mainMenu.ProgressListener;
import org.terasology.world.generator.WorldGenerator2DPreview;

/**
 * Creates a 2D preview based on {@link WorldGenerator2DPreview}.
 */
public class SummaryPreviewGenerator implements PreviewGenerator {

    private WorldGenerator2DPreview worldGenerator;
    private String layerName;

    public SummaryPreviewGenerator(WorldGenerator2DPreview worldGenerator, String layerName) {
        this.worldGenerator = worldGenerator;
        this.layerName = layerName;
    }

    @Override
    public ByteBuffer render(TextureData texData, int scale, ProgressListener progressListener) throws InterruptedException {
        int width = texData.getWidth();
        int height  = texData.getWidth();

        final int offX = -width / 2;
        final int offY = -height / 2;
        ByteBuffer buf = texData.getBuffers()[0];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int px = (x + offX) * scale;
                int py = (y + offY) * scale;
                Rect2i area = Rect2i.createFromMinAndSize(px, py, scale, scale);
                Color c = worldGenerator.get(layerName, area);
                c.addToBuffer(buf);
            }
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            if (progressListener != null) {
                progressListener.onProgress((float) y / height);
            }
        }
        buf.flip();
        return buf;
    }

    @Override
    public void close() {
        // nothing to do
    }
}
