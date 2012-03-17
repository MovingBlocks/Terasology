package org.terasology.componentSystem;

import org.terasology.entitySystem.ComponentSystem;

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface RenderSystem extends ComponentSystem {

    public void renderOpaque();
    public void renderTransparent();
    public void renderOverlay();
    public void renderFirstPerson();
}
