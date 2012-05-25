package org.terasology.model.shapes;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.logic.manager.AssetManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides access to block shapes by their title
 *
 * @author Immortius <immortius@gmail.com>
 */
public class BlockShapeManager {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private static final char SEPARATOR_CHAR = ':';
    private static final String DEFAULT_PACKAGE = "engine:";

    /* SINGLETON */
    private static BlockShapeManager instance;

    /* BLOCKS */
    private final HashMap<String, BlockShape> blockShapeByTitle = new HashMap<String, BlockShape>(128);

    public static BlockShapeManager getInstance() {
        if (instance == null)
            instance = new BlockShapeManager();
        return instance;
    }

    private BlockShapeManager() {
    }

    public void reload() {
        JsonBlockShapePersister persister = new JsonBlockShapePersister();
        blockShapeByTitle.clear();
        for (AssetUri shapeUri : AssetManager.getInstance().listAssets(AssetType.SHAPE)) {
            try {
                logger.log(Level.FINE, "Loading " + shapeUri.toString());
                BlockShape shape = persister.load(shapeUri.getPackage() + SEPARATOR_CHAR + shapeUri.getAssetName(), AssetManager.assetStream(shapeUri));
                blockShapeByTitle.put(shape.getTitle().toLowerCase(Locale.ENGLISH), shape);
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Failed to load " + shapeUri.toString(), ioe);
            }
        }
    }

    public BlockShape getBlockShape(String title) {
        if (title.indexOf(SEPARATOR_CHAR) == -1) {
            return blockShapeByTitle.get(DEFAULT_PACKAGE + title.toLowerCase(Locale.ENGLISH));
        }
        return blockShapeByTitle.get(title.toLowerCase(Locale.ENGLISH));
    }
}
