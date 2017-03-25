package org.terasology.rendering.cameras;

import org.terasology.config.RenderingConfig;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.RenderHelper;
import org.terasology.world.WorldProvider;

public abstract class SubmersibleCamera extends Camera {

    /* Used for Underwater Checks */
    private WorldProvider worldProvider;
    private RenderingConfig renderingConfig;

    public SubmersibleCamera(WorldProvider worldProvider, RenderingConfig renderingConfig) {
        this.worldProvider = worldProvider;
        this.renderingConfig = renderingConfig;
    }

    /**
     * Returns True if the head of the player is underwater. False otherwise.
     *
     * Takes in account waves if present.
     *
     * @return True if the head of the player is underwater. False otherwise.
     */
    public boolean isUnderWater() {
        // TODO: Making this as a subscribable value especially for node "ChunksRefractiveReflectiveNode",
        // TODO: glDisable and glEnable state changes on that node will be dynamically added/removed based on this value.
        Vector3f cameraPosition = new Vector3f(this.getPosition());

        // Compensate for waves
        if (renderingConfig.isAnimateWater()) {
            cameraPosition.y -= RenderHelper.evaluateOceanHeightAtPosition(cameraPosition, worldProvider.getTime().getDays());
        }

        if (worldProvider.isBlockRelevant(cameraPosition)) {
            return worldProvider.getBlock(cameraPosition).isLiquid();
        }
        return false;
    }
}
