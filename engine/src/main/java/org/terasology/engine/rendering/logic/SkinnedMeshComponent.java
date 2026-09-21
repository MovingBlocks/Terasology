// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.logic;

import com.google.common.collect.Maps;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.engine.entitySystem.Owns;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.rendering.assets.animation.MeshAnimation;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.SkinnedMesh;
import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.nui.Color;

import java.util.Map;

@ForceBlockActive
public class SkinnedMeshComponent implements VisualComponent<SkinnedMeshComponent> {
    @Replicate
    public SkinnedMesh mesh;

    @Replicate
    public Material material;

    @Replicate
    public MeshAnimation animation;

    public float currentTime;

    @Owns
    public Map<String, EntityRef> boneEntities;
    public EntityRef rootBone = EntityRef.NULL;

    @Replicate
    public float localScale = 1.0f;
    @Replicate
    public Vector3f localOffset = new Vector3f();
    @Replicate
    public Quaternionf localRotation = new Quaternionf();

    @Replicate
    public Color color = new Color(Color.white);

    @Override
    public void copyFrom(SkinnedMeshComponent other) {
        this.mesh = other.mesh;
        this.material = other.material;
        this.animation = other.animation;
        this.boneEntities = Maps.newHashMap(other.boneEntities);
        this.rootBone = other.rootBone;
        this.localScale = other.localScale;
        this.localOffset.set(other.localOffset);
        this.localRotation.set(other.localRotation);
        this.color = new Color(other.color);
    }
}
