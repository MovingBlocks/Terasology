package org.terasology.model.blocks.management

import org.terasology.model.blocks.Block

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface BlockLoader {
    Block loadBlock(ConfigObject blockConfig);
}