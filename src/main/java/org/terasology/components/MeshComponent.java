package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;

import javax.vecmath.Color4f;

/**
 *
 * @author Immortius <immortius@gmail.com>
 */
public final class MeshComponent extends AbstractComponent {

    // Temporary render details
    public enum RenderType {
        GelatinousCube
    }

    public String Name;
    public int ID;

    public RenderType renderType = RenderType.GelatinousCube;
    
    // TODO: Use some sort of mesh ref, that stores a direct reference to the mesh (flyweight pattern?)
    //public String mesh;
    // TODO: Some sort of Texture + Shader type?
    //public String material;
    
    // This should be elsewhere I think, probably in the material
    public Color4f color = new Color4f(0,0,0,1);

}
