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
package org.terasology.rendering.nui.asset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import org.terasology.classMetadata.ClassMetadata;
import org.terasology.classMetadata.FieldMetadata;
import org.terasology.classMetadata.copying.CopyStrategyLibrary;
import org.terasology.classMetadata.copying.strategy.Color4fCopyStrategy;
import org.terasology.classMetadata.copying.strategy.Quat4fCopyStrategy;
import org.terasology.classMetadata.copying.strategy.Vector2fCopyStrategy;
import org.terasology.classMetadata.copying.strategy.Vector3fCopyStrategy;
import org.terasology.classMetadata.copying.strategy.Vector3iCopyStrategy;
import org.terasology.classMetadata.copying.strategy.Vector4fCopyStrategy;
import org.terasology.classMetadata.reflect.ReflectFactory;
import org.terasology.classMetadata.reflect.ReflectionReflectFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.module.Module;
import org.terasology.math.Border;
import org.terasology.math.Rect2f;
import org.terasology.math.Rect2i;
import org.terasology.math.Region3i;
import org.terasology.math.Vector2i;
import org.terasology.math.Vector3i;
import org.terasology.persistence.ModuleContext;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.extensionTypes.AssetTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.TextureRegionTypeHandler;
import org.terasology.persistence.typeHandling.gson.JsonTypeHandlerAdapter;
import org.terasology.persistence.typeHandling.mathTypes.BorderTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Rect2fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Rect2iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Region3iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector2fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector2iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector3iTypeHandler;
import org.terasology.rendering.assets.TextureRegion;
import org.terasology.rendering.nui.LayoutHint;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UILayout;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.utilities.ReflectionUtil;
import org.terasology.utilities.gson.CaseInsensitiveEnumTypeAdapterFactory;

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
import java.util.List;

/**
 * @author Immortius
 */
public class UILoader implements AssetLoader<UIData> {

    public static final String CONTENTS_FIELD = "contents";
    public static final String LAYOUT_INFO_FIELD = "layoutInfo";

    private static final Logger logger = LoggerFactory.getLogger(UILoader.class);

    private TypeSerializationLibrary typeSerializationLibrary;


    @Override
    public UIData load(Module module, InputStream stream, List<URL> urls) throws IOException {
        NUIManager nuiManager = CoreRegistry.get(NUIManager.class);
        ReflectFactory reflectFactory = CoreRegistry.get(ReflectFactory.class);
        CopyStrategyLibrary copyStrategyLibrary = CoreRegistry.get(CopyStrategyLibrary.class);
        TypeSerializationLibrary library = new TypeSerializationLibrary(reflectFactory, copyStrategyLibrary);

        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new CaseInsensitiveEnumTypeAdapterFactory())
                .registerTypeAdapter(UIData.class, new UIDataTypeAdapter(nuiManager))
                .registerTypeHierarchyAdapter(UIWidget.class, new UIWidgetTypeAdapter(nuiManager))
                .registerTypeAdapter(Vector3i.class, new JsonTypeHandlerAdapter<>(new Vector3iTypeHandler()))
                .registerTypeAdapter(Region3i.class, new JsonTypeHandlerAdapter<>(new Region3iTypeHandler()))
                .registerTypeAdapter(Vector2i.class, new JsonTypeHandlerAdapter<>(new Vector2iTypeHandler()))
                .registerTypeAdapter(Rect2i.class, new JsonTypeHandlerAdapter<>(new Rect2iTypeHandler()))
                .registerTypeAdapter(Vector2f.class, new JsonTypeHandlerAdapter<>(new Vector2fTypeHandler()))
                .registerTypeAdapter(Rect2f.class, new JsonTypeHandlerAdapter<>(new Rect2fTypeHandler()))
                .registerTypeAdapter(UISkin.class, new JsonTypeHandlerAdapter<>(new AssetTypeHandler<>(AssetType.UI_SKIN, UISkin.class)))
                .registerTypeAdapter(Border.class, new JsonTypeHandlerAdapter<>(new BorderTypeHandler()))
                .registerTypeAdapter(TextureRegion.class, new JsonTypeHandlerAdapter<>(new TextureRegionTypeHandler()))
                .create();

        try (JsonReader reader = new JsonReader(new InputStreamReader(stream))) {
            reader.setLenient(true);
            return gson.fromJson(reader, UIData.class);
        }
    }

    private static final class UIDataTypeAdapter implements JsonDeserializer<UIData> {

        private NUIManager nuiManager;

        public UIDataTypeAdapter(NUIManager nuiManager) {
            this.nuiManager = nuiManager;
        }

        @Override
        public UIData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new UIData((UIWidget) context.deserialize(json, UIWidget.class));
        }
    }

    private static final class UIWidgetTypeAdapter implements JsonDeserializer<UIWidget> {

        private NUIManager nuiManager;

        public UIWidgetTypeAdapter(NUIManager nuiManager) {
            this.nuiManager = nuiManager;
        }

        @Override
        public UIWidget deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String type = jsonObject.get("type").getAsString();
            String id = null;
            if (jsonObject.has("id")) {
                id = jsonObject.get("id").getAsString();
            }
            ClassMetadata<? extends UIWidget, ?> elementMetadata = nuiManager.getElementMetadataLibrary().resolve(type, ModuleContext.getContext());
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

            for (FieldMetadata<? extends UIWidget, ?> field : elementMetadata.getFields()) {
                if (jsonObject.has(field.getName())) {
                    if (field.getName().equals(CONTENTS_FIELD) && UILayout.class.isAssignableFrom(elementMetadata.getType())) {
                        continue;
                    }
                    try {
                        field.setValue(element, context.deserialize(jsonObject.get(field.getName()), field.getType()));
                    } catch (Throwable e) {
                        logger.error("Failed to deserialize field {} of {}", field.getName(), type, e);
                    }
                }
            }
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
