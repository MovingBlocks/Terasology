/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.logic.behavior;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.terasology.HeadlessEnvironment;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.behavior.actions.LookupAction;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.core.ActionNode;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BehaviorTreeBuilder;
import org.terasology.logic.behavior.core.BehaviorTreeRunner;
import org.terasology.logic.location.LocationComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.logic.SkeletalMeshComponent;

public class LookupTest {
    @Before
    public void setup() {
        new HeadlessEnvironment();
    }

    @Test
    public void testEmptyLookup() {
        ActionNode node = new ActionNode(new LookupAction());
        System.out.println(CoreRegistry.get(BehaviorTreeBuilder.class).toJson(node));
    }

    @Test
    public void testLookupA() {
        Print.output = new StringBuilder();
        EntityRef entityRef = CoreRegistry.get(EntityManager.class).create(new LocationComponent(), new SkeletalMeshComponent());
        BehaviorTree asset = CoreRegistry.get(AssetManager.class).loadAsset(new AssetUri(AssetType.BEHAVIOR, "unittest", "BehaviorLookup"), BehaviorTree.class);
        System.out.println(CoreRegistry.get(BehaviorTreeBuilder.class).toJson(asset.getRoot()));
        Actor actor = new Actor(entityRef);
        actor.setDelta(0.5f);
        BehaviorTreeRunner runner = new DefaultBehaviorTreeRunner(asset, actor, null);
        runner.step();
        runner.step();
        runner.step();
        Assert.assertEquals("[A][A][A]", Print.output.toString());
    }

    @Test
    public void testLookupB() {
        Print.output = new StringBuilder();
        EntityRef entityRef = CoreRegistry.get(EntityManager.class).create(new LocationComponent(), new SkeletalMeshComponent());
        BehaviorTree asset = CoreRegistry.get(AssetManager.class).loadAsset(new AssetUri(AssetType.BEHAVIOR, "unittest", "BehaviorLookupB"), BehaviorTree.class);
        System.out.println(CoreRegistry.get(BehaviorTreeBuilder.class).toJson(asset.getRoot()));
        Actor actor = new Actor(entityRef);
        actor.setDelta(0.5f);
        BehaviorTreeRunner runner = new DefaultBehaviorTreeRunner(asset, actor, null);
        runner.step();
        runner.step();
        runner.step();
        Assert.assertEquals("[A][B][A][B][A][B]", Print.output.toString());
    }
}
