// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.asset;

import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.engine.logic.behavior.core.BehaviorNode;
import org.terasology.engine.logic.behavior.core.BehaviorTreeBuilder;
import org.terasology.engine.registry.CoreRegistry;

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
