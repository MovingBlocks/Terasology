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
package org.terasology.core.world.generator.trees;

import org.terasology.math.LSystemRule;
import org.terasology.world.block.BlockUri;

import com.google.common.collect.ImmutableMap;

/**
 * Creates trees based on the original
 */
public final class Trees {

    private Trees() {
        // no instances!
    }

    public static TreeGenerator oakTree() {
        return new TreeGeneratorLSystem(
            "FFFFFFA", ImmutableMap.<Character, LSystemRule>builder()
            .put('A', new LSystemRule("[&FFBFA]////[&BFFFA]////[&FBFFA]", 1.0f))
            .put('B', new LSystemRule("[&FFFA]////[&FFFA]////[&FFFA]", 0.8f)).build(),
            4, (float) Math.toRadians(30))
            .setLeafType(new BlockUri("core:GreenLeaf"))
            .setBarkType(new BlockUri("core:OakTrunk"));
    }

    public static TreeGenerator oakVariationTree() {
        return new TreeGeneratorLSystem(
            "FFFFFFA", ImmutableMap.<Character, LSystemRule>builder()
            .put('A', new LSystemRule("[&FFBFA]////[&BFFFA]////[&FBFFAFFA]", 1.0f))
            .put('B', new LSystemRule("[&FFFAFFFF]////[&FFFAFFF]////[&FFFAFFAA]", 0.8f)).build(),
            4, (float) Math.toRadians(35))
            .setLeafType(new BlockUri("core:GreenLeaf"))
            .setBarkType(new BlockUri("core:OakTrunk"));
    }

    public static TreeGenerator pineTree() {
        return new TreeGeneratorLSystem(
            "FFFFAFFFFFFFAFFFFA", ImmutableMap.<Character, LSystemRule>builder()
            .put('A', new LSystemRule("[&FFFFFA]////[&FFFFFA]////[&FFFFFA]", 1.0f)).build(),
            4, (float) Math.toRadians(35))
            .setLeafType(new BlockUri("core:DarkLeaf"))
            .setBarkType(new BlockUri("core:PineTrunk"));
    }

    public static TreeGenerator birchTree() {
        return new TreeGeneratorLSystem(
            "FFFFAFFFFBFFFFAFFFFBFFFFAFFFFBFF", ImmutableMap.<Character, LSystemRule>builder()
            .put('A', new LSystemRule("[&FFFAFFF]////[&FFAFFF]////[&FFFAFFF]", 1.0f))
            .put('B', new LSystemRule("[&FAF]////[&FAF]////[&FAF]", 0.8f)).build(), 4, (float) Math.toRadians(35))
            .setLeafType(new BlockUri("core:DarkLeaf"))
            .setBarkType(new BlockUri("core:BirchTrunk"));
    }

    public static TreeGenerator redTree() {
        return new TreeGeneratorLSystem("FFFFFAFAFAF", ImmutableMap.<Character, LSystemRule>builder()
            .put('A', new LSystemRule("[&FFAFF]////[&FFAFF]////[&FFAFF]", 1.0f)).build(),
            4, (float) Math.toRadians(40))
            .setLeafType(new BlockUri("core:RedLeaf"))
            .setBarkType(new BlockUri("core:OakTrunk"));
    }

    public static TreeGenerator cactus() {
        return new TreeGeneratorCactus()
            .setTrunkType(new BlockUri("core:Cactus"));
    }
}
