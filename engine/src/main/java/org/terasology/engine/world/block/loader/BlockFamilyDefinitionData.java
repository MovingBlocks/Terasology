// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.loader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.engine.world.block.family.AbstractBlockFamily;
import org.terasology.gestalt.assets.AssetData;
import org.terasology.context.annotation.API;

import java.util.List;
import java.util.Map;

@API
public class BlockFamilyDefinitionData implements AssetData {
    private boolean template;

    private SectionDefinitionData baseSection = new SectionDefinitionData();
    private Map<String, SectionDefinitionData> sections = Maps.newLinkedHashMap();
    private Class<? extends AbstractBlockFamily> family;

    private List<String> categories = Lists.newArrayList();

    public BlockFamilyDefinitionData() {

    }

    public BlockFamilyDefinitionData(BlockFamilyDefinitionData other) {
        baseSection = new SectionDefinitionData(other.getBaseSection());
        for (Map.Entry<String, SectionDefinitionData> entry : other.getSections().entrySet()) {
            sections.put(entry.getKey(), new SectionDefinitionData(entry.getValue()));
        }
        this.family = other.family;
        this.categories = Lists.newArrayList(other.categories);
    }

    public boolean isValid() {
        return family != null;
    }

    public boolean isTemplate() {
        return template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

    public SectionDefinitionData getBaseSection() {
        return baseSection;
    }

    public Map<String, SectionDefinitionData> getSections() {
        return sections;
    }

    public boolean hasSection(String section) {
        return sections.containsKey(section);
    }

    /**
     * @param section
     * @return Retrieves the data for the given section. If the section is not defined, retrieves the base section instead
     */
    public SectionDefinitionData getSection(String section) {
        SectionDefinitionData result = sections.get(section);
        if (result == null) {
            return baseSection;
        }
        return result;
    }

    public Class<? extends AbstractBlockFamily> getBlockFamily() {
        return family;
    }

    public void setBlockFamily(Class<? extends AbstractBlockFamily> family) {
        this.family = family;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
}
