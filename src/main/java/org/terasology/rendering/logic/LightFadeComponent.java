package org.terasology.rendering.logic;

import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;

/**
 * @author Immortius
 */
public final class LightFadeComponent implements Component {

    @Replicate
    public float targetDiffuseIntensity = 1.0f;

    @Replicate
    public float targetAmbientIntensity = 1.0f;

    @Replicate
    public boolean removeLightAfterFadeComplete;

    @Replicate
    public float diffuseFadeRate = 2.0f;

    @Replicate
    public float ambientFadeRate = 2.0f;

}
