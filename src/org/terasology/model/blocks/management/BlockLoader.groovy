package org.terasology.model.blocks.management

import org.terasology.model.blocks.Block
import org.terasology.math.Rotation

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface BlockLoader {
    Block loadBlock(ConfigObject blockConfig);
    Block loadBlock(ConfigObject blockConfig, Rotation rotation);
}