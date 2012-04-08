package org.terasology.model.shapes;

import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides acces to block shapes by their title
 *
 * @author Immortius <immortius@gmail.com>
 */
public class BlockShapeManager {

    private final Logger _logger = Logger.getLogger(getClass().getName());

    /* SINGLETON */
    private static BlockShapeManager _instance;

    /* GROOVY */
    private BlockShapeLoader _manifestor;

    /* BLOCKS */
    private final HashMap<String, BlockShape> _blockShapeByTitle = new HashMap<String, BlockShape>(128);

    public static BlockShapeManager getInstance() {
        if (_instance == null)
            _instance = new BlockShapeManager();

        return _instance;
    }

    private BlockShapeManager() {
        _manifestor = new BlockShapeLoader(this);
        loadBlockShapes();
    }

    private void loadBlockShapes() {
        try {
            _manifestor.loadShapes(); // Might have to catch plain Exception also for this step
        } catch (Exception e) {
            // TODO: Totally placeholder error handling, needs to be fancier
            _logger.log(Level.SEVERE, "Failed to load block shapes: ", e);
        }
    }

    public BlockShape getBlockShape(String title) {
        return _blockShapeByTitle.get(title.toLowerCase(Locale.ENGLISH));
    }

    public void addBlockShape(BlockShape shape) {
        _blockShapeByTitle.put(shape.getTitle().toLowerCase(Locale.ENGLISH), shape);
    }

    public void removeShape(BlockShape shape) {
        _blockShapeByTitle.remove(shape.getTitle().toLowerCase(Locale.ENGLISH));
    }

    public void addAllShapes(Iterable<BlockShape> shapes) {
        for (BlockShape b : shapes) {
            _blockShapeByTitle.put(b.getTitle().toLowerCase(Locale.ENGLISH), b);
        }
    }

}
