// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.fixtures;

import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestBlockManager extends BlockManager {

    private List<Block> blockList = new LinkedList<>();

    public TestBlockManager(Block... blocks) {
        this(Arrays.asList(blocks));
    }

    public TestBlockManager(List<Block> blocks) {
        this.blockList.addAll(blocks);
    }

    @Override
    public Map<String, Short> getBlockIdMap() {
        return blockList.stream().collect(Collectors.toMap(b -> b.getURI().toString(), Block::getId));
    }

    @Override
    public BlockFamily getBlockFamily(String uri) {
        return null;
    }

    @Override
    public BlockFamily getBlockFamily(BlockUri uri) {
        return null;
    }

    @Override
    public Block getBlock(String uri) {
        return getBlock(getBlockIdMap().get(uri));
    }

    @Override
    public Block getBlock(BlockUri uri) {
        return getBlock(uri.toString());
    }

    @Override
    public Block getBlock(short id) {
        return blockList.stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElse(null); // yeah it is ugly :'(
    }

    @Override
    public Collection<BlockUri> listRegisteredBlockUris() {
        return null;
    }

    @Override
    public Collection<BlockFamily> listRegisteredBlockFamilies() {
        return Collections.emptyList();
    }

    @Override
    public int getBlockFamilyCount() {
        return 0;
    }

    @Override
    public Collection<Block> listRegisteredBlocks() {
        return blockList;
    }
}
