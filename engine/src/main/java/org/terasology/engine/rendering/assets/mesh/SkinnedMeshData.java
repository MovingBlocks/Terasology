// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.terasology.engine.rendering.assets.mesh.resource.AllocationType;
import org.terasology.engine.rendering.assets.mesh.resource.DrawingMode;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexByteAttributeBinding;
import org.terasology.engine.rendering.assets.skeletalmesh.Bone;

import java.util.List;

public abstract class SkinnedMeshData extends MeshData {

    public SkinnedMeshData() {
        this(DrawingMode.TRIANGLES, AllocationType.STATIC);
    }

    public SkinnedMeshData(DrawingMode mode, AllocationType allocationType) {
        super(mode, allocationType);
    }

    public abstract VertexByteAttributeBinding boneIndex0();
    public abstract VertexByteAttributeBinding boneIndex1();
    public abstract VertexByteAttributeBinding boneIndex2();
    public abstract VertexByteAttributeBinding boneIndex3();
    public abstract VertexAttributeBinding<Vector4fc, Vector4f> weight();
    public abstract List<Bone> bones();

}
