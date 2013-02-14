/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

import java.util.EnumMap;
import java.util.List;

import javax.vecmath.Vector4f;

import org.terasology.world.block.BlockAdjacentType;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockEntityMode;
import org.terasology.world.block.BlockPart;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Immortius
 */
public class BlockDefinition {
    public String displayName = "";
    public boolean liquid = false;
    public boolean climbable = false;
    public boolean craftPlace = true;
    public byte hardness = 0x3;

    public boolean attachmentAllowed = true;
    public boolean replacementAllowed = false;
    public boolean supportRequired = false;

    public boolean penetrable = false;
    public boolean targetable = true;

    public boolean invisible = false;
    public boolean translucent = false;
    public boolean doubleSided = false;
    public boolean shadowCasting = true;
    public boolean waving = false;
    public boolean connectToAllBlock = false;
    public boolean checkHeightDiff = false;
    public List<String> acceptedToConnectBlocks = Lists.newArrayList();

    public byte luminance = 0;

    public List<String> categories = Lists.newArrayList();

    public String tile = "";
    public Tiles tiles;

    public Block.ColorSource colorSource = Block.ColorSource.DEFAULT;
    public ColorSources colorSources;

    public Vector4f colorOffset = new Vector4f(1, 1, 1, 1);
    public ColorOffsets colorOffsets;

    public float mass = 10f;
    public boolean debrisOnDestroy = true;

    public Entity entity;
    public Inventory inventory;

    public String shape = "";
    public List<String> shapes = Lists.newArrayList();
    public RotationType rotation = RotationType.NONE;

    public List<Type> types;

    public static enum RotationType {
        NONE,
        HORIZONTAL,
        ALIGNTOSURFACE,
        CONNECTTOADJACENT
    }

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
        public BlockEntityMode mode = BlockEntityMode.PERSISTENT;
    }

    public static class Inventory {
        public boolean directPickup = false;
        public boolean stackable = true;
    }

    public static class Type {
        public BlockAdjacentType type;
        public String shape;
        public List<String> shapes = Lists.newArrayList();
        public Tiles tiles;
    }
}
