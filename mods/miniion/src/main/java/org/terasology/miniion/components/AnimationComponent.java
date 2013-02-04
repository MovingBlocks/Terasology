/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.miniion.components;

import java.util.List;

import org.terasology.entitySystem.Component;
import org.terasology.rendering.assets.animation.MeshAnimation;

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
