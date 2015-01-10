/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.core.world.generator.chunkGenerators;

import org.terasology.math.LSystemRule;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.BlockManager;

import com.google.common.collect.ImmutableMap;

/**
 * Creates trees based on the original
 */
public final class Trees {

    private Trees() {
        // no instances!
    }

    public static TreeGenerator oakTree() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        return new TreeGeneratorLSystem(
            "FFFFFFA", ImmutableMap.<Character, LSystemRule>builder()
            .put('A', new LSystemRule("[&FFBFA]////[&BFFFA]////[&FBFFA]", 1.0f))
            .put('B', new LSystemRule("[&FFFA]////[&FFFA]////[&FFFA]", 0.8f)).build(),
            4, (float) Math.toRadians(30))
            .setLeafType(blockManager.getBlock("core:GreenLeaf"))
            .setBarkType(blockManager.getBlock("core:OakTrunk"));
    }

    public static TreeGenerator oakVariationTree() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        return new TreeGeneratorLSystem(
            "FFFFFFA", ImmutableMap.<Character, LSystemRule>builder()
            .put('A', new LSystemRule("[&FFBFA]////[&BFFFA]////[&FBFFAFFA]", 1.0f))
            .put('B', new LSystemRule("[&FFFAFFFF]////[&FFFAFFF]////[&FFFAFFAA]", 0.8f)).build(),
            4, (float) Math.toRadians(35))
            .setLeafType(blockManager.getBlock("core:GreenLeaf"))
            .setBarkType(blockManager.getBlock("core:OakTrunk"));
    }

    public static TreeGenerator pineTree() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        return new TreeGeneratorLSystem(
            "FFFFAFFFFFFFAFFFFA", ImmutableMap.<Character, LSystemRule>builder()
            .put('A', new LSystemRule("[&FFFFFA]////[&FFFFFA]////[&FFFFFA]", 1.0f)).build(),
            4, (float) Math.toRadians(35))
            .setLeafType(blockManager.getBlock("core:DarkLeaf"))
            .setBarkType(blockManager.getBlock("core:PineTrunk"));
    }

    public static TreeGenerator birkTree() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        return new TreeGeneratorLSystem(
            "FFFFAFFFFBFFFFAFFFFBFFFFAFFFFBFF", ImmutableMap.<Character, LSystemRule>builder()
            .put('A', new LSystemRule("[&FFFAFFF]////[&FFAFFF]////[&FFFAFFF]", 1.0f))
            .put('B', new LSystemRule("[&FAF]////[&FAF]////[&FAF]", 0.8f)).build(), 4, (float) Math.toRadians(35))
            .setLeafType(blockManager.getBlock("core:DarkLeaf"))
            .setBarkType(blockManager.getBlock("core:BirkTrunk"));
    }

    public static TreeGenerator redTree() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        return new TreeGeneratorLSystem("FFFFFAFAFAF", ImmutableMap.<Character, LSystemRule>builder()
            .put('A', new LSystemRule("[&FFAFF]////[&FFAFF]////[&FFAFF]", 1.0f)).build(),
            4, (float) Math.toRadians(40))
            .setLeafType(blockManager.getBlock("core:RedLeaf"))
            .setBarkType(blockManager.getBlock("core:OakTrunk"));
    }

    public static TreeGenerator cactus() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        return new TreeGeneratorCactus()
            .setTrunkType(blockManager.getBlock("core:Cactus"));
    }
}
