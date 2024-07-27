// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import com.google.common.collect.ImmutableSet;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.gestalt.assets.AssetDataProducer;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetDataProducer;
import org.terasology.context.annotation.API;
import org.terasology.gestalt.naming.Name;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@RegisterAssetDataProducer
@API
public class ScreenQuadMeshProducer implements AssetDataProducer<MeshData> {
    public static final Name SCREEN_QUAD_RESOURCE_NAME = new Name("ScreenQuad");

    @Override
    public Set<ResourceUrn> getAvailableAssetUrns() {
        return Collections.emptySet();
    }

    @Override
    public Set<Name> getModulesProviding(Name resourceName) {
        if (SCREEN_QUAD_RESOURCE_NAME.equals(resourceName)) {
            return ImmutableSet.of(TerasologyConstants.ENGINE_MODULE);
        }
        return Collections.emptySet();
    }

    @Override
    public ResourceUrn redirect(ResourceUrn urn) {
        return urn;
    }

    @Override
    public Optional<MeshData> getAssetData(ResourceUrn urn) throws IOException {
        if (TerasologyConstants.ENGINE_MODULE.equals(urn.getModuleName()) && SCREEN_QUAD_RESOURCE_NAME.equals(urn.getResourceName())) {
            StandardMeshData data = new StandardMeshData();
            Vector3f posDest = new Vector3f();
            Vector2f uvDest = new Vector2f();
            data.position.put(posDest.set(-1.0f, -1.0f, 0.0f));
            data.position.put(posDest.set(-1.0f, 1.0f, 0.0f));
            data.position.put(posDest.set(1.0f, 1.0f, 0.0f));
            data.position.put(posDest.set(1.0f, -1.0f, 0.0f));

            data.indices.put(0);
            data.indices.put(1);
            data.indices.put(2);

            data.indices.put(0);
            data.indices.put(2);
            data.indices.put(3);

            data.uv0.put(uvDest.set(0.0f, 0.0f));
            data.uv0.put(uvDest.set(0.0f, 1.0f));
            data.uv0.put(uvDest.set(1.0f, 1.0f));
            data.uv0.put(uvDest.set(1.0f, 0.0f));

            return Optional.of(data);
        }
        return Optional.empty();
    }
}
