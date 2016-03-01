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
package org.terasology.rendering.nui.skin;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.persistence.ModuleContext;
import org.terasology.reflection.metadata.ClassLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.utilities.gson.AssetTypeAdapter;
import org.terasology.utilities.gson.CaseInsensitiveEnumTypeAdapterFactory;
import org.terasology.utilities.gson.ColorTypeAdapter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 */
@RegisterAssetFileFormat
public class UISkinFormat extends AbstractAssetFileFormat<UISkinData> {

    private static final Logger logger = LoggerFactory.getLogger(UISkinFormat.class);
    private Gson gson;


    public UISkinFormat() {
        super("skin");
        gson = new GsonBuilder()
                .registerTypeAdapterFactory(new CaseInsensitiveEnumTypeAdapterFactory())
                .registerTypeAdapter(Font.class, new AssetTypeAdapter<>(Font.class))
                .registerTypeAdapter(UISkinData.class, new UISkinTypeAdapter())
                .registerTypeAdapter(TextureRegion.class, new TextureRegionTypeAdapter())
                .registerTypeAdapter(Color.class, new ColorTypeAdapter())
                .registerTypeAdapter(Optional.class, new OptionalTextureRegionTypeAdapter())
                .create();
    }

    @Override
    public UISkinData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(inputs.get(0).openStream(), Charsets.UTF_8))) {
            reader.setLenient(true);
            return gson.fromJson(reader, UISkinData.class);
        } catch (JsonParseException e) {
            throw new IOException("Failed to load skin '" + urn + "'", e);
        }
    }


    private static class TextureRegionTypeAdapter implements JsonDeserializer<TextureRegion> {

        @Override
        public TextureRegion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String uri = json.getAsString();
            return Assets.getTextureRegion(uri).orElse(null);
        }
    }

    private static class UISkinTypeAdapter implements JsonDeserializer<UISkinData> {
        @Override
        public UISkinData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonObject()) {
                UISkinBuilder builder = new UISkinBuilder();
                DefaultInfo defaultInfo = context.deserialize(json, DefaultInfo.class);
                defaultInfo.apply(builder);
                return builder.build();
            }
            return null;
        }
    }

    private static class DefaultInfo extends FamilyInfo {
        public String inherit;
        public Map<String, FamilyInfo> families;

        @Override
        public void apply(UISkinBuilder builder) {
            super.apply(builder);
            if (inherit != null) {
                Optional<? extends UISkin> skin = Assets.get(inherit, UISkin.class);
                if (skin.isPresent()) {
                    builder.setBaseSkin(skin.get());
                }
            }
            if (families != null) {
                for (Map.Entry<String, FamilyInfo> entry : families.entrySet()) {
                    builder.setFamily(entry.getKey());
                    entry.getValue().apply(builder);
                }
            }
        }
    }

    private static class FamilyInfo extends StyleInfo {
        public Map<String, ElementInfo> elements;

        public void apply(UISkinBuilder builder) {
            super.apply(builder);
            if (elements != null) {
                for (Map.Entry<String, ElementInfo> entry : elements.entrySet()) {
                    ClassLibrary<UIWidget> library = CoreRegistry.get(NUIManager.class).getWidgetMetadataLibrary();
                    ClassMetadata<? extends UIWidget, ?> metadata = library.resolve(entry.getKey(), ModuleContext.getContext());
                    if (metadata != null) {
                        builder.setElementClass(metadata.getType());
                        entry.getValue().apply(builder);
                    } else {
                        logger.warn("Failed to resolve UIWidget class {}, skipping style information", entry.getKey());
                    }


                }
            }
        }
    }

    private static class PartsInfo extends StyleInfo {
        public Map<String, StyleInfo> modes;

        public void apply(UISkinBuilder builder) {
            super.apply(builder);
            if (modes != null) {
                for (Map.Entry<String, StyleInfo> entry : modes.entrySet()) {
                    builder.setElementMode(entry.getKey());
                    entry.getValue().apply(builder);
                }
            }
        }
    }

    private static class ElementInfo extends StyleInfo {
        public Map<String, PartsInfo> parts;
        public Map<String, StyleInfo> modes;

        public void apply(UISkinBuilder builder) {
            super.apply(builder);
            if (modes != null) {
                for (Map.Entry<String, StyleInfo> entry : modes.entrySet()) {
                    builder.setElementMode(entry.getKey());
                    entry.getValue().apply(builder);
                }
            }
            if (parts != null) {
                for (Map.Entry<String, PartsInfo> entry : parts.entrySet()) {
                    builder.setElementPart(entry.getKey());
                    entry.getValue().apply(builder);
                }
            }
        }
    }

    private static class StyleInfo extends UIStyleFragment {

        private void apply(UISkinBuilder builder) {
            builder.setStyleFragment(this);
        }
    }

    private static class OptionalTextureRegionTypeAdapter implements JsonDeserializer<Optional<?>> {
        @Override
        public Optional<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Assets.getTextureRegion(json.getAsString());
        }
    }
}
