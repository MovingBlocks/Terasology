// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.asset;

import com.google.common.base.Charsets;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.logic.behavior.core.BehaviorNode;
import org.terasology.engine.logic.behavior.core.BehaviorTreeBuilder;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.gestalt.assets.format.AbstractAssetFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Loader for behavior assets. Can also save assets into json format.
 * <br><br>
 * If there are both, Nodes and Renderables tree, both are loaded/saved. To ensure, the nodes get associated to
 * the correct renderable, additional ids are introduced (only in the json file).
 * <br><br>
 *
 */
@RegisterAssetFileFormat
public class BehaviorTreeFormat extends AbstractAssetFileFormat<BehaviorTreeData> {
    private static final Logger logger = LoggerFactory.getLogger(BehaviorTreeFormat.class);


    public BehaviorTreeFormat() {
        super("behavior");
    }

    public void save(OutputStream stream, BehaviorTreeData data) throws IOException {
        BehaviorTreeBuilder builder = CoreRegistry.get(BehaviorTreeBuilder.class);
        OutputStreamWriter writer = new OutputStreamWriter(stream, Charsets.UTF_8);
        writer.write(builder.toJson(data.getRoot()));
        writer.close();
    }


    @Override
    public BehaviorTreeData load(ResourceUrn resourceUrn, List<AssetDataFile> list) throws IOException {
        BehaviorTreeBuilder builder = CoreRegistry.get(BehaviorTreeBuilder.class);
        if (builder == null) {
            builder = new BehaviorTreeBuilder();
            CoreRegistry.put(BehaviorTreeBuilder.class, builder);
        }
        try (InputStream stream = list.get(0).openStream()) {
            return load(stream);
        } catch (JsonParseException e) {
            // Gestalt only isolates a single asset's load failure (logs it, returns Optional.empty()) for
            // *checked* exceptions - an unchecked JsonParseException would instead propagate all the way out
            // and abort whatever triggered the load (e.g. crash the whole game on startup, see #5099).
            // Rethrowing as the IOException this method already declares routes it through that safety net.
            throw new IOException("Malformed behavior tree asset '" + resourceUrn + "': " + e.getMessage(), e);
        }
    }


    public BehaviorTreeData load(InputStream stream) {
        BehaviorTreeBuilder builder = CoreRegistry.get(BehaviorTreeBuilder.class);

        if (builder == null) {
            builder = new BehaviorTreeBuilder();
            CoreRegistry.put(BehaviorTreeBuilder.class, builder);
        }
        BehaviorNode node = builder.fromJson(stream);

        BehaviorTreeData data = new BehaviorTreeData();
        data.setRoot(node);
        return data;

    }
}
