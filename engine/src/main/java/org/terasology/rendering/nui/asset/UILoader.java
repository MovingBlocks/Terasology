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
package org.terasology.rendering.nui.asset;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetType;
import org.terasology.audio.Sound;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.engine.module.Module;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.math.Border;
import org.terasology.math.Rect2f;
import org.terasology.math.Rect2i;
import org.terasology.math.Region3i;
import org.terasology.math.Vector2i;
import org.terasology.math.Vector3i;
import org.terasology.persistence.ModuleContext;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.extensionTypes.AssetTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.BlockFamilyTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.BlockTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.CollisionGroupTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.Color4fTypeHanlder;
import org.terasology.persistence.typeHandling.extensionTypes.PrefabTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.TextureRegionTypeHandler;
import org.terasology.persistence.typeHandling.gson.JsonTypeHandlerAdapter;
import org.terasology.persistence.typeHandling.mathTypes.BorderTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Quat4fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Rect2fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Rect2iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Region3iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector2fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector2iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector3fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector3iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector4fTypeHandler;
import org.terasology.physics.CollisionGroup;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.LayoutHint;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UILayout;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.utilities.ReflectionUtil;
import org.terasology.utilities.gson.CaseInsensitiveEnumTypeAdapterFactory;
import org.terasology.world.block.Block;
import org.terasology.world.block.family.BlockFamily;

import javax.vecmath.Color4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

/**
 * The UILoader handles loading UI widgets from json format files.
 *
 * @author Immortius
 */
public class UILoader implements AssetLoader<UIData> {

    public static final String CONTENTS_FIELD = "contents";
    public static final String LAYOUT_INFO_FIELD = "layoutInfo";

    private static final Logger logger = LoggerFactory.getLogger(UILoader.class);


    @Override
    public UIData load(Module module, InputStream stream, List<URL> urls) throws IOException {
        NUIManager nuiManager = CoreRegistry.get(NUIManager.class);
        ReflectFactory reflectFactory = CoreRegistry.get(ReflectFactory.class);
        CopyStrategyLibrary copyStrategyLibrary = CoreRegistry.get(CopyStrategyLibrary.class);

        // TODO: Get this library from elsewhere
        TypeSerializationLibrary library = new TypeSerializationLibrary(reflectFactory, copyStrategyLibrary);
        library.add(BlockFamily.class, new BlockFamilyTypeHandler());
        library.add(Block.class, new BlockTypeHandler());
        library.add(Color4f.class, new Color4fTypeHanlder());
        library.add(Quat4f.class, new Quat4fTypeHandler());
        library.add(Texture.class, new AssetTypeHandler<>(AssetType.TEXTURE, Texture.class));
        library.add(Mesh.class, new AssetTypeHandler<>(AssetType.MESH, Mesh.class));
        library.add(Sound.class, new AssetTypeHandler<>(AssetType.SOUND, Sound.class));
        library.add(Material.class, new AssetTypeHandler<>(AssetType.MATERIAL, Material.class));
        library.add(SkeletalMesh.class, new AssetTypeHandler<>(AssetType.SKELETON_MESH, SkeletalMesh.class));
        library.add(MeshAnimation.class, new AssetTypeHandler<>(AssetType.ANIMATION, MeshAnimation.class));
        library.add(UISkin.class, new AssetTypeHandler<>(AssetType.UI_SKIN, UISkin.class));
        library.add(Vector4f.class, new Vector4fTypeHandler());
        library.add(Vector3f.class, new Vector3fTypeHandler());
        library.add(Vector2f.class, new Vector2fTypeHandler());
        library.add(Vector3i.class, new Vector3iTypeHandler());
        library.add(Vector2i.class, new Vector2iTypeHandler());
        library.add(Rect2i.class, new Rect2iTypeHandler());
        library.add(Rect2f.class, new Rect2fTypeHandler());
        library.add(CollisionGroup.class, new CollisionGroupTypeHandler());
        library.add(Region3i.class, new Region3iTypeHandler());
        library.add(Prefab.class, new PrefabTypeHandler());
        library.add(Border.class, new BorderTypeHandler());
        library.add(TextureRegion.class, new TextureRegionTypeHandler());

        GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeAdapterFactory(new CaseInsensitiveEnumTypeAdapterFactory())
                .registerTypeAdapter(UIData.class, new UIDataTypeAdapter())
                .registerTypeHierarchyAdapter(UIWidget.class, new UIWidgetTypeAdapter(nuiManager));
        for (Class<?> handledType : library.getCoreTypes()) {
            gsonBuilder.registerTypeAdapter(handledType, new JsonTypeHandlerAdapter<>(library.getHandlerFor(handledType)));
        }
        Gson gson = gsonBuilder.create();

        try (JsonReader reader = new JsonReader(new InputStreamReader(stream, Charsets.UTF_8))) {
            reader.setLenient(true);
            return gson.fromJson(reader, UIData.class);
        }
    }

    /**
     * Load UIData with a single, root widget
     */
    private static final class UIDataTypeAdapter implements JsonDeserializer<UIData> {

        @Override
        public UIData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new UIData((UIWidget) context.deserialize(json, UIWidget.class));
        }
    }

    /**
     * Loads a widget. This requires the following custom handling:
     * <ul>
     * <li>The class of the widget is determined through a URI in the "type" attribute</li>
     * <li>If the "id" attribute is present, it is passed to the constructor</li>
     * <li>If the widget is a layout, then a "contents" attribute provides a list of widgets for content.
     * Each contained widget may have a "layoutInfo" attribute providing the layout hint for its container.</li>
     * </ul>
     */
    private static final class UIWidgetTypeAdapter implements JsonDeserializer<UIWidget> {

        private NUIManager nuiManager;

        public UIWidgetTypeAdapter(NUIManager nuiManager) {
            this.nuiManager = nuiManager;
        }

        @Override
        public UIWidget deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            String type = jsonObject.get("type").getAsString();
            ClassMetadata<? extends UIWidget, ?> elementMetadata = nuiManager.getWidgetMetadataLibrary().resolve(type, ModuleContext.getContext());
            if (elementMetadata == null) {
                logger.error("Unknown UIWidget type {}", type);
                return null;
            }

            String id = null;
            if (jsonObject.has("id")) {
                id = jsonObject.get("id").getAsString();
            }

            UIWidget element;
            if (id != null) {
                try {
                    Constructor<? extends UIWidget> constructor = elementMetadata.getType().getConstructor(String.class);
                    constructor.setAccessible(true);
                    element = constructor.newInstance(id);

                } catch (NoSuchMethodException e) {
                    logger.warn("UIWidget type {} lacks id constructor", elementMetadata.getUri());
                    element = elementMetadata.newInstance();
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    logger.warn("Failed to construct {} with id", elementMetadata.getUri(), e);
                    element = elementMetadata.newInstance();
                }
            } else {
                element = elementMetadata.newInstance();
            }

            // Deserialize normal fields.
            for (FieldMetadata<? extends UIWidget, ?> field : elementMetadata.getFields()) {
                if (jsonObject.has(field.getSerializationName())) {
                    if (field.getName().equals(CONTENTS_FIELD) && UILayout.class.isAssignableFrom(elementMetadata.getType())) {
                        continue;
                    }
                    try {
                        if (List.class.isAssignableFrom(field.getType())) {
                            Type contentType = ReflectionUtil.getTypeParameter(field.getField().getGenericType(), 0);
                            if (contentType != null) {
                                List result = Lists.newArrayList();
                                JsonArray list = jsonObject.getAsJsonArray(field.getSerializationName());
                                for (JsonElement item : list) {
                                    result.add(context.deserialize(item, contentType));
                                }
                                field.setValue(element, result);
                            }
                        } else {
                            field.setValue(element, context.deserialize(jsonObject.get(field.getSerializationName()), field.getType()));
                        }
                    } catch (Throwable e) {
                        logger.error("Failed to deserialize field {} of {}", field.getName(), type, e);
                    }
                }
            }

            // Deserialize contents and layout hints
            if (UILayout.class.isAssignableFrom(elementMetadata.getType())) {
                UILayout layout = (UILayout) element;

                Class<? extends LayoutHint> layoutHintType = (Class<? extends LayoutHint>)
                        ReflectionUtil.getTypeParameter(elementMetadata.getType().getGenericSuperclass(), 0);
                if (jsonObject.has(CONTENTS_FIELD)) {
                    for (JsonElement child : jsonObject.getAsJsonArray(CONTENTS_FIELD)) {
                        UIWidget childElement = context.deserialize(child, UIWidget.class);
                        if (childElement != null) {
                            LayoutHint hint = null;
                            if (child.isJsonObject()) {
                                JsonObject childObject = child.getAsJsonObject();
                                if (layoutHintType != null && childObject.has(LAYOUT_INFO_FIELD)) {
                                    hint = context.deserialize(childObject.get(LAYOUT_INFO_FIELD), layoutHintType);
                                }
                            }
                            layout.addWidget(childElement, hint);
                        }
                    }
                }
            }
            return element;
        }
    }
}
