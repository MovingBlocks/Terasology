package org.terasology.miniion.components;

import java.util.List;

import org.terasology.entitySystem.Component;
import org.terasology.rendering.assets.animation.MeshAnimation;

public class AnimationComponent implements Component{
	public MeshAnimation walkAnim; // Different speeds?
	public MeshAnimation idleAnim; // combine with randomanims
	public MeshAnimation attackAnim; // Different speeds?
	public MeshAnimation dieAnim; // same as byebye, wrath of god (destroy all minions)
	public MeshAnimation fadeInAnim; //Teleport at location, also spawnanimation
	public MeshAnimation fadeOutAnim; //Teleport to location 
	public MeshAnimation workAnim;
	
	public List<MeshAnimation> randomAnim; // random animations while Idle

}
