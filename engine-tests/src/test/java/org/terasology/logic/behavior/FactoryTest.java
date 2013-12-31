/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior;

import org.junit.Assert;
import org.junit.Test;
import org.terasology.asset.AssetManager;
import org.terasology.engine.CoreRegistry;
import org.terasology.logic.behavior.asset.BehaviorTreeData;
import org.terasology.logic.behavior.asset.BehaviorTreeLoader;
import org.terasology.logic.behavior.tree.CounterNode;
import org.terasology.logic.behavior.tree.MonitorNode;
import org.terasology.logic.behavior.tree.ParallelNode;
import org.terasology.logic.behavior.tree.RepeatNode;
import org.terasology.logic.behavior.tree.SequenceNode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import static org.mockito.Mockito.mock;

/**
 * @author synopia
 */
public class FactoryTest {
    @Test
    public void testSaveLoad() throws IOException {
        AssetManager assetManager = mock(AssetManager.class);
        CoreRegistry.put(AssetManager.class, assetManager);
        BehaviorTreeLoader loader = new BehaviorTreeLoader();

        BehaviorTreeData data = buildSample();

        OutputStream os = new ByteArrayOutputStream(10000);
        loader.save(os, data);
        String jsonExpected = os.toString();

        data = loader.load(null, new ByteArrayInputStream(jsonExpected.getBytes()), null);
        os = new ByteArrayOutputStream(10000);
        loader = new BehaviorTreeLoader();
        loader.save(os, data);
        String jsonActual = os.toString();
        Assert.assertEquals(jsonActual, jsonExpected);
        System.out.println(jsonActual);
    }

    private BehaviorTreeData buildSample() {
        SequenceNode sequence = new SequenceNode();
        sequence.children().add(new CounterNode(1));
        sequence.children().add(new RepeatNode(new CounterNode(2)));
        ParallelNode parallel = new ParallelNode(ParallelNode.Policy.RequireAll, ParallelNode.Policy.RequireAll);
        sequence.children().add(parallel);
        parallel.children().add(new MonitorNode());
        parallel.children().add(new CounterNode(3));
        BehaviorTreeData tree = new BehaviorTreeData();
        tree.setRoot(sequence);
        tree.createRenderable(new BehaviorNodeFactory(new ArrayList<BehaviorNodeComponent>()));
        return tree;
    }
}
