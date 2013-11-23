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
import org.terasology.math.Rect2f;
import org.terasology.persistence.ModuleContext;
import org.terasology.rendering.assets.TextureRegion;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.Border;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.UIElement;
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
            if (uri.isEmpty()) {
                return new NullTextureRegion();
            }
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
        public Map<String, ElementInfo> elements;

        public void apply(UISkinBuilder builder) {
            super.apply(builder);
            if (elements != null) {
                for (Map.Entry<String, ElementInfo> entry : elements.entrySet()) {
                    ClassLibrary<UIElement> library = CoreRegistry.get(NUIManager.class).getElementMetadataLibrary();
                    ClassMetadata<? extends UIElement, ?> metadata = library.resolve(entry.getKey(), ModuleContext.getContext());
                    if (metadata != null) {
                        builder.setElementClass(metadata.getType());
                        entry.getValue().apply(builder);
                    } else {
                        logger.warn("Failed to resolve UIElement class {}, skipping style information", entry.getKey());
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
                for (Map.Entry<String, PartsInfo> entry : parts.entrySet())  {
                    builder.setElementPart(entry.getKey());
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
        @SerializedName("background-auto-draw")
        public Boolean backgroundAutomaticallyDrawn;

        public Border margin;
        @SerializedName("fixed-width")
        public Integer fixedWidth;
        @SerializedName("fixed-height")
        public Integer fixedHeight;
        @SerializedName("align-horizontal")
        public HorizontalAlign alignmentH;
        @SerializedName("align-vertical")
        public VerticalAlign alignmentV;

        @SerializedName("texture-scale-mode")
        public ScaleMode textureScaleMode;

        public Font font;
        @SerializedName("text-color")
        public Color textColor;
        @SerializedName("text-shadow-color")
        public Color textShadowColor;

        @SerializedName("text-align-horizontal")
        public HorizontalAlign textAlignmentH;
        @SerializedName("text-align-vertical")
        public VerticalAlign textAlignmentV;
        @SerializedName("text-shadowed")
        public Boolean textShadowed;

        public void apply(UISkinBuilder builder) {
            if (background != null) {
                if (background.getRegion().isEmpty()) {
                    builder.setBackground(null);
                } else {
                    builder.setBackground(background);
                }
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
            if (backgroundAutomaticallyDrawn != null) {
                builder.setBackgroundAutomaticallyDrawn(backgroundAutomaticallyDrawn);
            }
            if (fixedWidth != null) {
                builder.setFixedWidth(fixedWidth);
            }
            if (fixedHeight != null) {
                builder.setFixedHeight(fixedHeight);
            }
            if (alignmentH != null) {
                builder.setHorizontalAlignment(alignmentH);
            }
            if (alignmentV != null) {
                builder.setVerticalAlignment(alignmentV);
            }
        }
    }

    private static class NullTextureRegion implements TextureRegion {

        @Override
        public Texture getTexture() {
            return null;
        }

        @Override
        public Rect2f getRegion() {
            return Rect2f.EMPTY;
        }

        @Override
        public int getWidth() {
            return 0;
        }

        @Override
        public int getHeight() {
            return 0;
        }
    }
}
