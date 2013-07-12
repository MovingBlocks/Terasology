package org.terasology.ligthandshadow.components;

import org.terasology.entitySystem.Component;
import org.terasology.rendering.assets.animation.MeshAnimation;

import java.util.List;

/**
 *
 */
public class AnimationComponent implements Component {
    public MeshAnimation walkAnim; // Different speeds?
    public MeshAnimation idleAnim; // combine with randomanims
    public MeshAnimation attackAnim; // Different speeds?
    // same as byebye, wrath of god (destroy all minions)
    public MeshAnimation dieAnim;

    // Teleport at location, also spawnanimation
    public MeshAnimation fadeInAnim;

    public MeshAnimation fadeOutAnim; // Teleport to location
    public MeshAnimation workAnim;
    public MeshAnimation terraformAnim;

    public List<MeshAnimation> randomAnim; // random animations while Idle

}
