package org.terasology.entitySystem.componentSystem;

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface RenderSystem extends ComponentSystem {

    public void renderOpaque();
    public void renderTransparent();
    public void renderOverlay();
    public void renderFirstPerson();
}
