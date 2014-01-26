
package org.terasology.rendering;

import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;

public class ShaderManagerHeadless implements ShaderManager {

    private Material activeMaterial;

    @Override
    public void initShaders() {
    }

    @Override
    public void setActiveMaterial(Material material) {
        activeMaterial = material;
    }

    @Override
    public void bindTexture(int slot, Texture texture) {
        // Do nothing
    }

    @Override
    public Material getActiveMaterial() {
        return activeMaterial;
    }

    @Override
    public void recompileAllShaders() {
        // Do nothing
    }

    @Override
    public void enableDefault() {
        // Do nothing
    }

    @Override
    public void enableDefaultTextured() {
        // Do nothing
    }

    @Override
    public void disableShader() {
        // Do nothing
    }

}
