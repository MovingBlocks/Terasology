package org.terasology.components.rendering;

import org.terasology.entitySystem.Component;
import org.terasology.rendering.assets.Material;
import org.terasology.rendering.primitives.Mesh;

import javax.vecmath.Color4f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class MeshComponent implements Component {

    // Temporary render details
    public enum RenderType {
        Normal,
        GelatinousCube
    }

    public RenderType renderType = RenderType.Normal;
    public Mesh mesh;
    public Material material;

    // TODO: Some sort of Texture + Shader type?
    //public String material;

    // This should be elsewhere I think, probably in the material
    public Color4f color = new Color4f(0, 0, 0, 1);

}
