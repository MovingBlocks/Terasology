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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import org.terasology.math.Rect2i;
import org.terasology.math.Region3i;
import org.terasology.math.geom.ImmutableVector2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.rendering.nui.layers.mainMenu.ProgressListener;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.World;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.worldviewer.color.ColorModels;
import org.terasology.worldviewer.layers.FacetLayer;
import org.terasology.worldviewer.layers.engine.SurfaceHeightFacetLayer;

import com.google.common.math.IntMath;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class FacetLayerPreview implements PreviewGenerator {

    private static final int TILE_SIZE_X = ChunkConstants.SIZE_X * 2;
    private static final int TILE_SIZE_Y = ChunkConstants.SIZE_Z * 2;

    private final DirectColorModel colorModel = ColorModels.RGBA;

    private WorldGenerator worldGenerator;

    /**
     * @param worldGenerator
     */
    public FacetLayerPreview(WorldGenerator worldGenerator) {
        this.worldGenerator = worldGenerator;
    }

    @Override
    public ByteBuffer create(int width, int height, int scale, ProgressListener progressListener) throws InterruptedException {
        final int offX = -width * scale / 2;
        final int offY = -height * scale / 2;

        Rect2i tileArea = worldToTileArea(Rect2i.createFromMinAndSize(offX, offY, width * scale, height * scale));
        int tileIdx = 0;
        float tileCount = tileArea.area();


        int[] masks = colorModel.getMasks();
        DataBufferInt imageBuffer = new DataBufferInt(width * height);
        WritableRaster raster = Raster.createPackedRaster(imageBuffer, width, height, width, masks, null);
        BufferedImage view = new BufferedImage(colorModel, raster, false, null);

        Graphics2D g = view.createGraphics();
        g.scale(1f/scale, 1f/scale);
        g.translate(-offX, -offY);


        for (int z = tileArea.minY(); z < tileArea.maxY(); z++) {
            for (int x = tileArea.minX(); x < tileArea.maxX(); x++) {
                ImmutableVector2i pos = new ImmutableVector2i(x, z);
                Region region = createRegion(pos);
                BufferedImage image = rasterize(region);
                g.drawImage(image, x * TILE_SIZE_X, z * TILE_SIZE_Y, null);

                tileIdx++;
                if (progressListener != null) {
                    progressListener.onProgress(tileIdx / tileCount);
                }
            }
        }

        g.dispose();

        int[] data = imageBuffer.getData();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(data.length * Integer.BYTES);
        byteBuffer.asIntBuffer().put(data);

        return byteBuffer;
    }

    private Region createRegion(ImmutableVector2i chunkPos) {

        int vertChunks = 4; // 4 chunks high (relevant for trees, etc)

        int minX = chunkPos.getX() * TILE_SIZE_X;
        int minZ = chunkPos.getY() * TILE_SIZE_Y;
        int height = vertChunks * ChunkConstants.SIZE_Y;
        Region3i area3d = Region3i.createFromMinAndSize(new Vector3i(minX, 0, minZ), new Vector3i(TILE_SIZE_X, height, TILE_SIZE_Y));
        World world = worldGenerator.getWorld();
        Region region = world.getWorldData(area3d);
        return region;
    }

    private static Rect2i worldToTileArea(Rect2i area) {
        int chunkMinX = IntMath.divide(area.minX(), TILE_SIZE_X, RoundingMode.FLOOR);
        int chunkMinZ = IntMath.divide(area.minY(), TILE_SIZE_Y, RoundingMode.FLOOR);

        int chunkMaxX = IntMath.divide(area.maxX(), TILE_SIZE_X, RoundingMode.CEILING);
        int chunkMaxZ = IntMath.divide(area.maxY(), TILE_SIZE_Y, RoundingMode.CEILING);

        return Rect2i.createFromMinAndMax(chunkMinX, chunkMinZ, chunkMaxX, chunkMaxZ);
    }

    /**
     * Note: this method must be thread-safe!
     * @param region the thread-safe region
     * @return an image of that region
     */
    private BufferedImage rasterize(Region region) {

        Vector3i extent = region.getRegion().size();
        int width = extent.x;
        int height = extent.z;

        WritableRaster raster = colorModel.createCompatibleWritableRaster(width, height);
        BufferedImage image = new BufferedImage(colorModel, raster, false, null);

        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        FacetLayer layer = new SurfaceHeightFacetLayer();

        try {
             layer.render(image, region);
        } finally {
            g.dispose();
        }

        return image;
    }

}
