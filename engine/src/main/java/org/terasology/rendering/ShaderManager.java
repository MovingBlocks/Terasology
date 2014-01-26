package org.terasology.rendering;

import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;

public interface ShaderManager {

    public void initShaders();

    public void setActiveMaterial(Material material);

    public void bindTexture(int slot, Texture texture);

    public Material getActiveMaterial();

    public void recompileAllShaders();

    /**
     * Enables the default shader program.
     */
    public void enableDefault();

    /**
     * Enables the default shader program.
     */
    public void enableDefaultTextured();

    public void disableShader();

}