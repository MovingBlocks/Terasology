/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.rendering.logic;

import java.util.Map;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.rendering.assets.Material;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;

/**
 * @author Immortius
 */
public class SkeletalMeshComponent implements Component {
    public SkeletalMesh mesh;
    public Material material;
    public MeshAnimation animation;
    public boolean loop = false;
    public float animationRate = 1.0f;

    public Map<String, EntityRef> boneEntities;
    public EntityRef rootBone = EntityRef.NULL;
    public float animationTime = 0;

}
