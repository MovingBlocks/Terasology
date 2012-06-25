/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.logic.tools;

/**
 * Can be used to create and place blueprints.
 */
/*public class BlueprintTool extends SimpleTool {

    private final ArrayList<BlockPosition> _selectedBlocks = new ArrayList<BlockPosition>();

    public BlueprintTool(Player player) {
        super(player);
    }

    public void executeLeftClickAction() {
        RayBlockIntersection.Intersection selectedBlock = _player.getSelectedBlock();

        if (ItemBlueprint.class.isInstance(_player.getActiveItem()) && selectedBlock != null) {
            ItemBlueprint bpItem = (ItemBlueprint) _player.getActiveItem();

            if (bpItem.getBlueprint() == null) {
                addBlock(selectedBlock.getBlockPosition());
            } else {
                bpItem.getBlueprint().build(_player.getParent().getWorldProvider(), selectedBlock.getBlockPosition());
            }
        }
    }

    public void executeRightClickAction() {
        if (ItemBlueprint.class.isInstance(_player.getActiveItem())) {
            ItemBlueprint bpItem = (ItemBlueprint) _player.getActiveItem();
            bpItem.setBlueprint(null);
        }
    }

    private void addBlock(BlockPosition blockPosition) {
        Terasology.getInstance().getActiveWorldRenderer().getBlockGrid().addGridPosition(blockPosition);
        _selectedBlocks.add(blockPosition);

        if (_selectedBlocks.size() >= 2) {
            generateBlueprint();
            Terasology.getInstance().getActiveWorldRenderer().getBlockGrid().clear();
            _selectedBlocks.clear();
        }
    }

    private void generateBlueprint() {
        if (_selectedBlocks.size() < 2)
            return;

        int maxX = Math.max(_selectedBlocks.get(0).x, _selectedBlocks.get(1).x);
        int minX = Math.min(_selectedBlocks.get(0).x, _selectedBlocks.get(1).x);
        int maxY = Math.max(_selectedBlocks.get(0).y, _selectedBlocks.get(1).y);
        int minY = Math.min(_selectedBlocks.get(0).y, _selectedBlocks.get(1).y);
        int maxZ = Math.max(_selectedBlocks.get(0).z, _selectedBlocks.get(1).z);
        int minZ = Math.min(_selectedBlocks.get(0).z, _selectedBlocks.get(1).z);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (_player.getParent().getWorldProvider().getBlock(x, y, z) != 0x0) {
                        BlockPosition bp = new BlockPosition(x, y, z);

                        _selectedBlocks.add(bp);
                        Terasology.getInstance().getActiveWorldRenderer().getBlockGrid().addGridPosition(bp);
                    }
                }
            }
        }

        if (ItemBlueprint.class.isInstance(_player.getActiveItem())) {
            ItemBlueprint bpItem = (ItemBlueprint) _player.getActiveItem();
            bpItem.setBlueprint(BlueprintManager.getInstance().generateBlueprint(_player.getParent().getWorldProvider(), _selectedBlocks));
        }
    }
}      */
