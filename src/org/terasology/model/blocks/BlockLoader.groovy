package org.terasology.model.blocks

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface BlockLoader {
    Block loadBlock(ConfigObject blockConfig);
}