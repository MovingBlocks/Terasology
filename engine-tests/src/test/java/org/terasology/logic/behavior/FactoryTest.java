/*
 * Copyright 2014 MovingBlocks
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

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.management.AssetManager;
import org.terasology.engine.module.ModuleManager;
import org.terasology.logic.behavior.asset.BehaviorTreeData;
import org.terasology.logic.behavior.asset.BehaviorTreeFormat;
import org.terasology.logic.behavior.asset.NodesClassLibrary;
import org.terasology.logic.behavior.tree.MonitorNode;
import org.terasology.logic.behavior.tree.ParallelNode;
import org.terasology.logic.behavior.tree.RepeatNode;
import org.terasology.logic.behavior.tree.SequenceNode;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.testUtil.ModuleManagerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author synopia
 */
public class FactoryTest {
    @Test
    public void testSaveLoad() throws IOException {
        AssetManager assetManager = mock(AssetManager.class);
        CoreRegistry.put(AssetManager.class, assetManager);
        BehaviorNodeFactory nodeFactory = mock(BehaviorNodeFactory.class);
        CoreRegistry.put(BehaviorNodeFactory.class, nodeFactory);
        BehaviorTreeFormat loader = new BehaviorTreeFormat();
        BehaviorTreeData data = buildSample();

        ByteArrayOutputStream os = new ByteArrayOutputStream(10000);
        loader.save(os, data);
        byte[] jsonExpected = os.toByteArray();

        AssetDataFile assetDataFile = mock(AssetDataFile.class);
        when(assetDataFile.openStream()).thenReturn(new BufferedInputStream(new ByteArrayInputStream(jsonExpected)));

        data = loader.load(null, Lists.newArrayList(assetDataFile));
        os = new ByteArrayOutputStream(10000);
        loader = new BehaviorTreeFormat();
        loader.save(os, data);
        byte[] jsonActual = os.toByteArray();
        Assert.assertArrayEquals(jsonActual, jsonExpected);
    }

    private BehaviorTreeData buildSample() {
        SequenceNode sequence = new SequenceNode();
        sequence.children().add(new DebugNode(1));
        sequence.children().add(new RepeatNode(new DebugNode(2)));
        ParallelNode parallel = new ParallelNode(ParallelNode.Policy.RequireAll, ParallelNode.Policy.RequireAll);
        sequence.children().add(parallel);
        parallel.children().add(new MonitorNode());
        parallel.children().add(new DebugNode(3));
        BehaviorTreeData tree = new BehaviorTreeData();
        tree.setRoot(sequence);
        tree.createRenderable();
        tree.layout(null);
        return tree;
    }

    @Before
    public void setup() throws Exception {
        ModuleManager moduleManager = ModuleManagerFactory.create();
        ReflectionReflectFactory reflectFactory = new ReflectionReflectFactory();
        CoreRegistry.put(ReflectFactory.class, reflectFactory);
        CopyStrategyLibrary copyStrategies = new CopyStrategyLibrary(reflectFactory);
        CoreRegistry.put(CopyStrategyLibrary.class, copyStrategies);
        CoreRegistry.put(ModuleManager.class, moduleManager);
        NodesClassLibrary nodesClassLibrary = new NodesClassLibrary(reflectFactory, copyStrategies);
        CoreRegistry.put(NodesClassLibrary.class, nodesClassLibrary);
        nodesClassLibrary.scan(moduleManager.getEnvironment());
    }
}
