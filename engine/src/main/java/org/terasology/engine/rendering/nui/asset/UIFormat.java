// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.asset;

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
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.core.module.ModuleContext;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.persistence.typeHandling.extensionTypes.AssetTypeHandler;
import org.terasology.engine.persistence.typeHandling.extensionTypes.UISkinTypeHandler;
import org.terasology.engine.persistence.typeHandling.gson.GsonTypeSerializationLibraryAdapterFactory;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.utilities.ReflectionUtil;
import org.terasology.engine.utilities.gson.CaseInsensitiveEnumTypeAdapterFactory;
import org.terasology.gestalt.assets.format.AbstractAssetFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.nui.LayoutHint;
import org.terasology.nui.UILayout;
import org.terasology.nui.UIWidget;
import org.terasology.nui.asset.UIData;
import org.terasology.nui.skin.UISkin;
import org.terasology.nui.skin.UISkinAsset;
import org.terasology.nui.widgets.UILabel;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 * Handles loading UI widgets from json format files.
 */
@RegisterAssetFileFormat
public class UIFormat extends AbstractAssetFileFormat<UIData> {

    public static final String CONTENTS_FIELD = "contents";
    public static final String LAYOUT_INFO_FIELD = "layoutInfo";
    public static final String ID_FIELD = "id";
    public static final String TYPE_FIELD = "type";
    public static final String ENABLED_FIELD = "enabled";

    private static final Logger logger = LoggerFactory.getLogger(UIFormat.class);

    public UIFormat() {
        super("ui");
    }

    @Override
    public UIData load(ResourceUrn resourceUrn, List<AssetDataFile> inputs) throws IOException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(inputs.get(0).openStream(), Charsets.UTF_8))) {
            reader.setLenient(true);
            UIData data = load(new JsonParser().parse(reader));
            data.setSource(inputs.get(0));
            return data;
        }
    }

    public UIData load(JsonElement element) throws IOException {
        return load(element, null);
    }

    public UIData load(JsonElement element, Locale otherLocale) throws IOException {
        NUIManager nuiManager = CoreRegistry.get(NUIManager.class);
        TranslationSystem translationSystem = CoreRegistry.get(TranslationSystem.class);
        TypeHandlerLibrary library = CoreRegistry.get(TypeHandlerLibrary.class).copy();
        library.addTypeHandler(UISkinAsset.class, new AssetTypeHandler<>(UISkinAsset.class));
        library.addTypeHandler(UISkin.class, new UISkinTypeHandler());

        // TODO: Rewrite to use TypeHandlerLibrary

        GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeAdapterFactory(new GsonTypeSerializationLibraryAdapterFactory(library))
                .registerTypeAdapterFactory(new CaseInsensitiveEnumTypeAdapterFactory())
                .registerTypeAdapter(UIData.class, new UIDataTypeAdapter())
                .registerTypeHierarchyAdapter(UIWidget.class, new UIWidgetTypeAdapter(nuiManager));

        // override the String TypeAdapter from the serialization library
        gsonBuilder.registerTypeAdapter(String.class, new I18nStringTypeAdapter(translationSystem, otherLocale));
        Gson gson = gsonBuilder.create();
        return gson.fromJson(element, UIData.class);
    }

    private static final class I18nStringTypeAdapter implements JsonDeserializer<String> {

        private final TranslationSystem translationSystem;
        private final Locale otherLocale;

        I18nStringTypeAdapter(TranslationSystem translationSystem, Locale otherLocale) {
            this.translationSystem = translationSystem;
            this.otherLocale = otherLocale;
        }

        @Override
        public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String text = json.getAsString();
            return otherLocale == null
                ? translationSystem.translate(text) : translationSystem.translate(text, otherLocale);
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

        UIWidgetTypeAdapter(NUIManager nuiManager) {
            this.nuiManager = nuiManager;
        }

        @Override
        public UIWidget deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                return new UILabel(json.getAsString());
            }

            JsonObject jsonObject = json.getAsJsonObject();

            String type = jsonObject.get(TYPE_FIELD).getAsString();
            ClassMetadata<? extends UIWidget, ?> elementMetadata = nuiManager.getWidgetMetadataLibrary().resolve(type, ModuleContext.getContext());
            if (elementMetadata == null) {
                logger.error("Unknown UIWidget type {}", type);
                return null;
            }

            String id = null;
            if (jsonObject.has(ID_FIELD)) {
                id = jsonObject.get(ID_FIELD).getAsString();
            }

            UIWidget element = elementMetadata.newInstance();
            if (id != null) {
                FieldMetadata<?, ?> fieldMetadata = elementMetadata.getField(ID_FIELD);
                if (fieldMetadata == null) {
                    logger.warn("UIWidget type {} lacks id field", elementMetadata.getId()); //NOPMD
                } else {
                    fieldMetadata.setValue(element, id);
                }
            }

            // Deserialize normal fields.
            Set<String> unknownFields = new HashSet<>();
            for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String name = entry.getKey();
                if (!ID_FIELD.equals(name)
                    && !CONTENTS_FIELD.equals(name)
                    && !TYPE_FIELD.equals(name)
                    && !LAYOUT_INFO_FIELD.equals(name)) {
                    unknownFields.add(name);
                }
            }

            for (FieldMetadata<? extends UIWidget, ?> field : elementMetadata.getFields()) {
                if (jsonObject.has(field.getSerializationName())) {
                    unknownFields.remove(field.getSerializationName());
                    if (field.getName().equals(CONTENTS_FIELD) && UILayout.class.isAssignableFrom(elementMetadata.getType())) {
                        continue;
                    }
                    // added new if block instead of modifying previous block conditions to avoid ruining code readability
                    if (field.getName().equals(ENABLED_FIELD) && UILayout.class.isAssignableFrom(elementMetadata.getType())) {
                        continue;
                    }
                    try {
                        if (List.class.isAssignableFrom(field.getType())) {
                            Type contentType = ReflectionUtil.getTypeParameter(field.getField().getGenericType(), 0);
                            if (contentType != null) {
                                List<Object> result = Lists.newArrayList();
                                JsonArray list = jsonObject.getAsJsonArray(field.getSerializationName());
                                for (JsonElement item : list) {
                                    result.add(context.deserialize(item, contentType));
                                }
                                field.setValue(element, result);
                            }
                        } else {
                            field.setValue(element, context.deserialize(jsonObject.get(field.getSerializationName()), field.getType()));
                        }
                    } catch (RuntimeException e) {
                        logger.error("Failed to deserialize field {} of {}", field.getName(), type, e); //NOPMD
                    }
                }
            }

            for (String key : unknownFields) {
                logger.warn("Field '{}' not recognized for {} in {}", key, typeOfT, json);
            }

            // Deserialize contents and layout hints
            if (UILayout.class.isAssignableFrom(elementMetadata.getType())) {
                UILayout<LayoutHint> layout = (UILayout<LayoutHint>) element;

                Class<? extends LayoutHint> layoutHintType = (Class<? extends LayoutHint>)
                        ReflectionUtil.getTypeParameter(elementMetadata.getType().getGenericSuperclass(), 0);
                if (jsonObject.has(CONTENTS_FIELD)) {
                    for (JsonElement child : jsonObject.getAsJsonArray(CONTENTS_FIELD)) {
                        UIWidget childElement = context.deserialize(child, UIWidget.class);
                        if (childElement != null) {
                            LayoutHint hint = null;
                            if (child.isJsonObject()) {
                                JsonObject childObject = child.getAsJsonObject();
                                if (layoutHintType != null && !layoutHintType.isInterface() && !Modifier.isAbstract(layoutHintType.getModifiers())
                                        && childObject.has(LAYOUT_INFO_FIELD)) {
                                    hint = context.deserialize(childObject.get(LAYOUT_INFO_FIELD), layoutHintType);
                                }
                            }
                            layout.addWidget(childElement, hint);
                        }
                    }
                }
                if (jsonObject.has(ENABLED_FIELD)) {
                    FieldMetadata<? extends UIWidget, ?> enabledField = elementMetadata.getField(ENABLED_FIELD);
                    enabledField.setValue(element,
                            context.deserialize(jsonObject.get(enabledField.getSerializationName()),
                                    enabledField.getType()));
                }
            }

            //This allows screens to have access to nuiManager after editing .ui file
            if (element instanceof CoreScreenLayer && Objects.isNull(((CoreScreenLayer) element).getManager())) {
                ((CoreScreenLayer) element).setManager(nuiManager);
            }
            return element;
        }
    }
}
