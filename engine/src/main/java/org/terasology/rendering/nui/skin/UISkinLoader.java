/*
 * Copyright 2013 MovingBlocks
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetType;
import org.terasology.asset.Assets;
import org.terasology.classMetadata.ClassLibrary;
import org.terasology.classMetadata.ClassMetadata;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.module.Module;
import org.terasology.persistence.ModuleContext;
import org.terasology.rendering.assets.TextureRegion;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.Border;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.VerticalAlign;
import org.terasology.utilities.gson.AssetTypeAdapter;
import org.terasology.utilities.gson.CaseInsensitiveEnumTypeAdapterFactory;
import org.terasology.utilities.gson.ColorTypeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author Immortius
 */
public class UISkinLoader implements AssetLoader<UISkinData> {

    private static final Logger logger = LoggerFactory.getLogger(UISkinLoader.class);
    private Gson gson;

    public UISkinLoader() {
        gson = new GsonBuilder()
                .registerTypeAdapterFactory(new CaseInsensitiveEnumTypeAdapterFactory())
                .registerTypeAdapter(Font.class, new AssetTypeAdapter<Font>(AssetType.FONT))
                .registerTypeAdapter(UISkinData.class, new UISkinTypeAdapter())
                .registerTypeAdapter(TextureRegion.class, new TextureRegionTypeAdapter())
                .registerTypeAdapter(Color.class, new ColorTypeAdapter())
                .create();
    }

    @Override
    public UISkinData load(Module module, InputStream stream, List<URL> urls) throws IOException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(stream))) {
            reader.setLenient(true);
            return gson.fromJson(reader, UISkinData.class);
        }
    }

    private static class TextureRegionTypeAdapter implements JsonDeserializer<TextureRegion> {

        @Override
        public TextureRegion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String uri = json.getAsString();
            return Assets.getTextureRegion(uri);
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
        public Map<String, FamilyInfo> families;

        public void apply(UISkinBuilder builder) {
            super.apply(builder);
            if (families != null) {
                for (Map.Entry<String, FamilyInfo> entry : families.entrySet()) {
                    builder.setFamily(entry.getKey());
                    entry.getValue().apply(builder);
                }
            }
        }
    }

    private static class FamilyInfo extends StyleInfo {
        public Map<String, WidgetInfo> widgets;

        public void apply(UISkinBuilder builder) {
            super.apply(builder);
            if (widgets != null) {
                for (Map.Entry<String, WidgetInfo> entry : widgets.entrySet()) {
                    ClassLibrary<UIWidget> library = CoreRegistry.get(NUIManager.class).getWidgetMetadataLibrary();
                    ClassMetadata<? extends UIWidget, ?> metadata = library.resolve(entry.getKey(), ModuleContext.getContext());
                    if (metadata != null) {
                        builder.setWidgetClass(metadata.getType());
                        entry.getValue().apply(builder);
                    } else {
                        logger.warn("Failed to resolve widget class {}, skipping style information", entry.getKey());
                    }


                }
            }
        }
    }

    private static class WidgetInfo extends StyleInfo {
        public Map<String, StyleInfo> modes;

        public void apply(UISkinBuilder builder) {
            super.apply(builder);
            if (modes != null) {
                for (Map.Entry<String, StyleInfo> entry : modes.entrySet()) {
                    builder.setWidgetMode(entry.getKey());
                    entry.getValue().apply(builder);
                }
            }
        }
    }

    private static class StyleInfo {
        public TextureRegion background;
        @SerializedName("background-border")
        public Border backgroundBorder;
        @SerializedName("background-scale-mode")
        public ScaleMode backgroundScaleMode;

        public Border margin;

        @SerializedName("texture-scale-mode")
        public ScaleMode textureScaleMode;

        public Font font;
        @SerializedName("text-color")
        public Color textColor;
        @SerializedName("text-shadow-color")
        public Color textShadowColor;

        @SerializedName("text-horizontal-alignment")
        public HorizontalAlign textAlignmentH;
        @SerializedName("text-vertical-alignment")
        public VerticalAlign textAlignmentV;
        @SerializedName("text-shadowed")
        public Boolean textShadowed;

        public void apply(UISkinBuilder builder) {
            if (background != null) {
                builder.setBackground(background);
            }
            if (backgroundBorder != null) {
                builder.setBackgroundBorder(backgroundBorder);
            }
            if (backgroundScaleMode != null) {
                builder.setBackgroundMode(backgroundScaleMode);
            }
            if (margin != null) {
                builder.setMargin(margin);
            }
            if (textureScaleMode != null) {
                builder.setTextureScaleMode(textureScaleMode);
            }
            if (font != null) {
                builder.setFont(font);
            }
            if (textColor != null) {
                builder.setTextColor(textColor);
            }
            if (textShadowColor != null) {
                builder.setTextShadowColor(textShadowColor);
            }
            if (textAlignmentH != null) {
                builder.setTextHorizontalAlignment(textAlignmentH);
            }
            if (textAlignmentV != null) {
                builder.setTextVerticalAlignment(textAlignmentV);
            }
            if (textShadowed != null) {
                builder.setTextShadowed(textShadowed);
            }
        }
    }
}
