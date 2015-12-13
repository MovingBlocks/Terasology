/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.world.block.loader;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.terasology.assets.Asset;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;
import org.terasology.utilities.gson.CaseInsensitiveEnumTypeAdapterFactory;
import org.terasology.utilities.gson.Vector3fTypeAdapter;
import org.terasology.utilities.gson.Vector4fTypeAdapter;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.DefaultColorSource;
import org.terasology.world.block.family.BlockFamilyFactory;
import org.terasology.world.block.family.BlockFamilyFactoryRegistry;
import org.terasology.world.block.family.FreeformBlockFamilyFactory;
import org.terasology.world.block.family.HorizontalBlockFamilyFactory;
import org.terasology.world.block.family.MultiSection;
import org.terasology.world.block.family.SymmetricBlockFamilyFactory;
import org.terasology.world.block.shapes.BlockShape;
import org.terasology.world.block.sounds.BlockSounds;
import org.terasology.world.block.tiles.BlockTile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 */
public class BlockFamilyDefinitionFormat extends AbstractAssetFileFormat<BlockFamilyDefinitionData> {

    private static final ResourceUrn DEFAULT_SOUNDS = new ResourceUrn("engine", "default");

    private final BlockFamilyFactory symmetricFamily = new SymmetricBlockFamilyFactory();
    private final BlockFamilyFactory horizontalFamily = new HorizontalBlockFamilyFactory();
    private final BlockFamilyFactory freeformFamily = new FreeformBlockFamilyFactory();
    private final AssetManager assetManager;
    private final Gson gson;

    public BlockFamilyDefinitionFormat(AssetManager assetManager, BlockFamilyFactoryRegistry blockFamilyFactoryRegistry) {
        super("block");
        this.assetManager = assetManager;
        gson = new GsonBuilder()
                .registerTypeAdapterFactory(new CaseInsensitiveEnumTypeAdapterFactory())
                .registerTypeAdapterFactory(new AssetTypeAdapterFactory(assetManager))
                .registerTypeAdapter(BlockFamilyDefinitionData.class, new BlockFamilyDefinitionDataHandler())
                .registerTypeAdapter(Vector3f.class, new Vector3fTypeAdapter())
                .registerTypeAdapter(Vector4f.class, new Vector4fTypeAdapter())
                .registerTypeAdapter(BlockFamilyFactory.class, new BlockFamilyFactoryHandler(blockFamilyFactoryRegistry))
                .create();
    }

    @Override
    public BlockFamilyDefinitionData load(ResourceUrn resourceUrn, List<AssetDataFile> input) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input.get(0).openStream(), Charsets.UTF_8))) {
            BlockFamilyDefinitionData data = gson.fromJson(reader, BlockFamilyDefinitionData.class);

            applyDefaults(resourceUrn, data.getBaseSection());
            data.getSections().values().stream().forEach(section -> applyDefaults(resourceUrn, section));
            if (!data.isTemplate()) {
                if (data.getFamilyFactory() == null && data.getBaseSection().getShape() != null) {
                    if (data.getBaseSection().getShape().isCollisionYawSymmetric()) {
                        data.setFamilyFactory(symmetricFamily);
                    } else {
                        data.setFamilyFactory(horizontalFamily);
                    }
                } else if (data.getFamilyFactory() == null) {
                    data.setFamilyFactory(freeformFamily);
                }
            }

            return data;
        }
    }

    private void applyDefaults(ResourceUrn resourceUrn, SectionDefinitionData section) {
        Optional<BlockTile> defaultTile = assetManager.getAsset(resourceUrn, BlockTile.class);
        if (defaultTile.isPresent()) {
            for (BlockPart part : BlockPart.values()) {
                if (section.getBlockTiles().get(part) == null) {
                    section.getBlockTiles().put(part, defaultTile.get());
                }
            }
        }
        if (section.getSounds() == null) {
            section.setSounds(assetManager.getAsset(DEFAULT_SOUNDS, BlockSounds.class).get());
        }

    }

    private class BlockFamilyDefinitionDataHandler implements JsonDeserializer<BlockFamilyDefinitionData> {

        private Type listOfStringType = new TypeToken<List<String>>() {
        }.getType();


        @Override
        public BlockFamilyDefinitionData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            BlockFamilyDefinitionData base = createBaseData(jsonObject);

            // Deserialize everything
            BlockFamilyDefinitionData result = new BlockFamilyDefinitionData(base);
            setBoolean(result::setTemplate, jsonObject, "template");
            setObject(result::setFamilyFactory, jsonObject, "rotation", BlockFamilyFactory.class, context);
            setObject(result::setCategories, jsonObject, "categories", listOfStringType, context);

            deserializeSectionDefinitionData(result.getBaseSection(), jsonObject, context);

            if (result.getFamilyFactory() != null) {
                for (MultiSection multiSection : result.getFamilyFactory().getMultiSections()) {
                    if (jsonObject.has(multiSection.getName()) && jsonObject.get(multiSection.getName()).isJsonObject()) {
                        JsonObject jsonMultiSection = jsonObject.getAsJsonObject(multiSection.getName());
                        for (String section : multiSection.getAppliesToSections()) {
                            SectionDefinitionData sectionData = result.getSections().get(section);
                            if (sectionData == null) {
                                sectionData = new SectionDefinitionData(base.getSection(section));
                                deserializeSectionDefinitionData(sectionData, jsonObject, context);
                                result.getSections().put(section, sectionData);
                            }
                            deserializeSectionDefinitionData(sectionData, jsonMultiSection, context);
                        }
                    }
                }

                for (String section : result.getFamilyFactory().getSectionNames()) {
                    if (jsonObject.has(section) && jsonObject.get(section).isJsonObject()) {
                        SectionDefinitionData sectionData = result.getSections().get(section);
                        if (sectionData == null) {
                            sectionData = new SectionDefinitionData(base.getSection(section));
                            deserializeSectionDefinitionData(sectionData, jsonObject, context);
                            result.getSections().put(section, sectionData);
                        }
                        deserializeSectionDefinitionData(sectionData, jsonObject.getAsJsonObject(section), context);
                    }
                }
            }

            return result;
        }

        private void deserializeSectionDefinitionData(SectionDefinitionData data, JsonObject jsonObject, JsonDeserializationContext context) {
            setString(data::setDisplayName, jsonObject, "displayName");
            setBoolean(data::setLiquid, jsonObject, "liquid");
            setInt(data::setHardness, jsonObject, "hardness");
            setBoolean(data::setAttachmentAllowed, jsonObject, "attachmentAllowed");
            setBoolean(data::setReplacementAllowed, jsonObject, "replacementAllowed");
            setBoolean(data::setSupportRequired, jsonObject, "supportRequired");
            setBoolean(data::setPenetrable, jsonObject, "penetrable");
            setBoolean(data::setTargetable, jsonObject, "targetable");
            setBoolean(data::setClimbable, jsonObject, "climbable");
            setBoolean(data::setInvisible, jsonObject, "invisible");
            setBoolean(data::setTranslucent, jsonObject, "translucent");
            setBoolean(data::setDoubleSided, jsonObject, "doubleSided");
            setBoolean(data::setShadowCasting, jsonObject, "shadowCasting");
            setBoolean(data::setWaving, jsonObject, "waving");
            setObject(data::setSounds, jsonObject, "sounds", BlockSounds.class, context);
            setByte(data::setLuminance, jsonObject, "luminance");
            setObject(data::setTint, jsonObject, "tint", Vector3f.class, context);

            readBlockPartMap(jsonObject, "tile", "tiles", data::getBlockTiles, BlockTile.class, context);
            readBlockPartMap(jsonObject, "colorSource", "colorSources", data::getColorSources, DefaultColorSource.class, context);
            readBlockPartMap(jsonObject, "colorOffset", "colorOffsets", data::getColorOffsets, Vector4f.class, context);

            setFloat(data::setMass, jsonObject, "mass");
            setBoolean(data::setDebrisOnDestroy, jsonObject, "debrisOnDestroy");

            if (jsonObject.has("entity") && jsonObject.get("entity").isJsonObject()) {
                JsonObject entityObject = jsonObject.getAsJsonObject("entity");
                setObject(data.getEntity()::setPrefab, entityObject, "prefab", Prefab.class, context);
                setBoolean(data.getEntity()::setKeepActive, entityObject, "keepActive");
            }

            if (jsonObject.has("inventory") && jsonObject.get("inventory").isJsonObject()) {
                JsonObject inventoryObject = jsonObject.getAsJsonObject("inventory");
                setBoolean(data.getInventory()::setDirectPickup, inventoryObject, "directPickup");
                setBoolean(data.getInventory()::setStackable, inventoryObject, "stackable");
            }

            setObject(data::setShape, jsonObject, "shape", BlockShape.class, context);
            setBoolean(data::setWater, jsonObject, "water");
            setBoolean(data::setLava, jsonObject, "lava");
            setBoolean(data::setGrass, jsonObject, "grass");
            setBoolean(data::setIce, jsonObject, "ice");
        }

        private <T> void readBlockPartMap(JsonObject jsonObject, String singleName,
                                          String partsName, Supplier<EnumMap<BlockPart, T>> supplier, Class<T> type, JsonDeserializationContext context) {
            if (jsonObject.has(singleName)) {
                T value = context.deserialize(jsonObject.get(singleName), type);
                for (BlockPart blockPart : BlockPart.values()) {
                    supplier.get().put(blockPart, value);
                }
            }
            if (jsonObject.has(partsName) && jsonObject.get(partsName).isJsonObject()) {
                JsonObject partsObject = jsonObject.getAsJsonObject(partsName);
                if (partsObject.has("all")) {
                    T value = context.deserialize(partsObject.get("all"), type);
                    for (BlockPart blockPart : BlockPart.values()) {
                        supplier.get().put(blockPart, value);
                    }
                }
                if (partsObject.has("sides")) {
                    T value = context.deserialize(partsObject.get("sides"), type);
                    for (BlockPart blockPart : BlockPart.horizontalSides()) {
                        supplier.get().put(blockPart, value);
                    }
                }
                if (partsObject.has("topBottom")) {
                    T value = context.deserialize(partsObject.get("topBottom"), type);
                    supplier.get().put(BlockPart.TOP, value);
                    supplier.get().put(BlockPart.BOTTOM, value);
                }
                for (BlockPart part : BlockPart.values()) {
                    String partName = part.toString().toLowerCase(Locale.ENGLISH);
                    if (partsObject.has(partName)) {
                        T value = context.deserialize(partsObject.get(partName), type);
                        supplier.get().put(part, value);
                    }
                }
            }
        }

        private void setString(Consumer<String> setter, JsonObject jsonObject, String name) {
            JsonPrimitive primitive = jsonObject.getAsJsonPrimitive(name);
            if (primitive != null) {
                setter.accept(primitive.getAsString());
            }
        }

        private void setBoolean(Consumer<Boolean> setter, JsonObject jsonObject, String name) {
            JsonPrimitive primitive = jsonObject.getAsJsonPrimitive(name);
            if (primitive != null && primitive.isBoolean()) {
                setter.accept(primitive.getAsBoolean());
            }
        }

        private void setInt(Consumer<Integer> setter, JsonObject jsonObject, String name) {
            JsonPrimitive primitive = jsonObject.getAsJsonPrimitive(name);
            if (primitive != null && primitive.isNumber()) {
                setter.accept(primitive.getAsInt());
            }
        }

        private void setFloat(Consumer<Float> setter, JsonObject jsonObject, String name) {
            JsonPrimitive primitive = jsonObject.getAsJsonPrimitive(name);
            if (primitive != null && primitive.isNumber()) {
                setter.accept(primitive.getAsFloat());
            }
        }

        private void setByte(Consumer<Byte> setter, JsonObject jsonObject, String name) {
            JsonPrimitive primitive = jsonObject.getAsJsonPrimitive(name);
            if (primitive != null && primitive.isNumber()) {
                setter.accept(primitive.getAsByte());
            }
        }

        @SuppressWarnings("unchecked")
        private <T> void setObject(Consumer<T> setter, JsonObject jsonObject, String name, Type type, JsonDeserializationContext context) {
            JsonElement object = jsonObject.get(name);
            if (object != null) {
                setter.accept(context.deserialize(object, type));
            }
        }

        private BlockFamilyDefinitionData createBaseData(JsonObject jsonObject) {
            JsonPrimitive basedOn = jsonObject.getAsJsonPrimitive("basedOn");
            if (basedOn != null && !basedOn.getAsString().isEmpty()) {
                Optional<BlockFamilyDefinition> baseDef = assetManager.getAsset(basedOn.getAsString(), BlockFamilyDefinition.class);
                if (baseDef.isPresent()) {
                    BlockFamilyDefinitionData data = baseDef.get().getData();
                    if (data.getFamilyFactory() instanceof FreeformBlockFamilyFactory) {
                        data.setFamilyFactory(null);
                    }
                    return data;
                } else {
                    throw new JsonParseException("Unable to resolve based block definition '" + basedOn.getAsString() + "'");
                }
            }
            BlockFamilyDefinitionData data = new BlockFamilyDefinitionData();
            data.getBaseSection().setSounds(assetManager.getAsset("engine:default", BlockSounds.class).get());
            return data;
        }
    }

    private static class BlockFamilyFactoryHandler implements JsonDeserializer<BlockFamilyFactory> {

        private final BlockFamilyFactoryRegistry blockFamilyFactoryRegistry;

        public BlockFamilyFactoryHandler(BlockFamilyFactoryRegistry blockFamilyFactoryRegistry) {
            this.blockFamilyFactoryRegistry = blockFamilyFactoryRegistry;
        }

        @Override
        public BlockFamilyFactory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return blockFamilyFactoryRegistry.getBlockFamilyFactory(json.getAsString());
        }
    }

    private static class AssetTypeAdapterFactory implements TypeAdapterFactory {

        private final AssetManager assetManager;

        public AssetTypeAdapterFactory(AssetManager assetManager) {
            this.assetManager = assetManager;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            Class<T> rawType = (Class<T>) type.getRawType();
            if (Asset.class.isAssignableFrom(rawType)) {
                final Class<? extends Asset> assetClass = (Class<? extends Asset>) rawType;
                return (TypeAdapter) new TypeAdapter<Asset>() {
                    @Override
                    public void write(JsonWriter out, Asset value) throws IOException {
                        if (value == null) {
                            out.nullValue();
                        } else {
                            out.value(value.getUrn().toString());
                        }
                    }

                    @Override
                    public Asset read(JsonReader in) throws IOException {
                        if (in.peek() == JsonToken.NULL) {
                            in.nextNull();
                            return null;
                        } else {
                            String value = in.nextString();
                            Optional<? extends Asset> asset = assetManager.getAsset(value, assetClass);
                            if (asset.isPresent()) {
                                return asset.get();
                            }
                        }
                        return null;
                    }
                };
            }
            return null;
        }
    }

}

