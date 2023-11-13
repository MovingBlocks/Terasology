// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.family;

import com.google.common.collect.Sets;
import org.terasology.engine.world.block.BlockBuilderHelper;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;
import org.terasology.engine.world.block.shapes.BlockShape;

import java.util.Locale;
import java.util.Set;

public abstract class AbstractBlockFamily implements BlockFamily {

    private BlockUri uri;
    private Set<String> categories = Sets.newHashSet();

    protected AbstractBlockFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        setBlockUri(new BlockUri(definition.getUrn()));
        setCategory(definition.getCategories());
    }

    protected AbstractBlockFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        setBlockUri(new BlockUri(definition.getUrn()));
        setCategory(definition.getCategories());
    }

    protected void setCategory(Iterable<String> newCategories) {
        for (String category : newCategories) {
            this.categories.add(category.toLowerCase(Locale.ENGLISH));
        }
    }

    protected void setBlockUri(BlockUri newUri) {
        uri = newUri;
    }


    @Override
    public BlockUri getURI() {
        return uri;
    }

    @Override
    public String getDisplayName() {
        return getArchetypeBlock().getDisplayName();
    }

    @Override
    public Iterable<String> getCategories() {
        return categories;
    }

    @Override
    public boolean hasCategory(String category) {
        return categories.contains(category.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public String toString() {
        String familyType = "";
        RegisterBlockFamily registerInfo = this.getClass().getAnnotation(RegisterBlockFamily.class);
        if (registerInfo != null) {
            familyType = registerInfo.value();
        }
        return "BlockFamily[" + familyType + "," + uri.toString() + "]";
    }
}
