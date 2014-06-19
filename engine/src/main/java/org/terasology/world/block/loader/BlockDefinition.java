/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.world.block.loader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.engine.API;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockPart;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.EnumMap;
import java.util.List;

/**
 * @author Immortius
 */
// add API so that modules can define Block Families
@API
public class BlockDefinition {
    public String displayName = "";
    public boolean liquid;
    public byte hardness = 0x3;

    public boolean attachmentAllowed = true;
    public boolean replacementAllowed;
    public boolean supportRequired;

    public boolean penetrable;
    public boolean targetable = true;
    public boolean climbable;

    public boolean invisible;
    public boolean translucent;
    public boolean doubleSided;
    public boolean shadowCasting = true;
    public boolean waving;

    public byte luminance;

    public Vector3f tint = new Vector3f();

    public List<String> categories = Lists.newArrayList();

    public String tile = "";
    public Tiles tiles;

    public Block.ColorSource colorSource = Block.ColorSource.DEFAULT;
    public ColorSources colorSources;

    public Vector4f colorOffset = new Vector4f(1, 1, 1, 1);
    public ColorOffsets colorOffsets;

    public float mass = 10f;
    public boolean debrisOnDestroy = true;
    public float viscosity = 0f;

    public Entity entity;
    public Inventory inventory;

    public String shape = "";
    public List<String> shapes = Lists.newArrayList();
    public String rotation;
    public boolean water;
    public boolean lava;
    public boolean grass;
    public boolean ice;

    public static class Tiles {
        public EnumMap<BlockPart, String> map = Maps.newEnumMap(BlockPart.class);
    }

    public static class ColorSources {
        public EnumMap<BlockPart, Block.ColorSource> map = Maps.newEnumMap(BlockPart.class);
    }

    public static class ColorOffsets {
        public EnumMap<BlockPart, Vector4f> map = Maps.newEnumMap(BlockPart.class);
    }

    public static class Entity {
        public String prefab = "";
        public boolean keepActive;
    }

    public static class Inventory {
        public boolean directPickup;
        public boolean stackable = true;
    }
}
