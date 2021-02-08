// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.joml.Vector4fc;
import org.joml.Vector4ic;
import org.reflections.Reflections;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.joml.geom.AABBf;
import org.terasology.joml.geom.AABBi;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.math.IntegerRange;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.math.geom.Vector4f;
import org.terasology.naming.Name;
import org.terasology.nui.Color;
import org.terasology.nui.Colorc;
import org.terasology.nui.UITextureRegion;
import org.terasology.persistence.typeHandling.extensionTypes.ColorTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.ColorcTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.NameTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.PrefabTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.TextureRegionTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.UITextureRegionTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.factories.AssetTypeHandlerFactory;
import org.terasology.persistence.typeHandling.extensionTypes.factories.ComponentClassTypeHandlerFactory;
import org.terasology.persistence.typeHandling.extensionTypes.factories.TextureRegionAssetTypeHandlerFactory;
import org.terasology.persistence.typeHandling.mathTypes.AABBfTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.AABBiTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.BlockAreaTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.BlockAreacTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.BlockRegionTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.IntegerRangeHandler;
import org.terasology.persistence.typeHandling.mathTypes.QuaternionfTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.QuaternionfcTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.RectanglefTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.RectangleiTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector2fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector2fcTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector2iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector2icTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector3fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector3fcTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector3iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector3icTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector4fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector4fcTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector4iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector4icTypeHandler;
import org.terasology.persistence.typeHandling.reflection.ModuleEnvironmentSandbox;
import org.terasology.persistence.typeHandling.reflection.SerializationSandbox;
import org.terasology.reflection.TypeRegistry;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.world.block.BlockArea;
import org.terasology.world.block.BlockAreac;
import org.terasology.world.block.BlockRegion;

/**
 * A library of type handlers. This is used for the construction of class metadata. This library should be initialised
 * by adding a number of base type handlers, describing how to serialize each supported type. It will then produce
 * serializers for classes (through their ClassMetadata) on request.
 */
public class TypeHandlerLibraryImpl extends TypeHandlerLibrary {


    public TypeHandlerLibraryImpl(Reflections reflections) {
        super(reflections);
        addTypeHandlerFactory(new ComponentClassTypeHandlerFactory());
    }

    protected TypeHandlerLibraryImpl(SerializationSandbox sandbox) {
        super(sandbox);
        addTypeHandlerFactory(new ComponentClassTypeHandlerFactory());
    }

    public TypeHandlerLibraryImpl(ModuleManager moduleManager, TypeRegistry typeRegistry) {
        super(new ModuleEnvironmentSandbox(moduleManager, typeRegistry));
    }


    public static TypeHandlerLibrary withReflections(Reflections reflections) {
        TypeHandlerLibrary library = new TypeHandlerLibraryImpl(reflections);

        populateWithDefaultHandlers(library);

        return library;
    }

    public static TypeHandlerLibrary forModuleEnvironment(ModuleManager moduleManager, TypeRegistry typeRegistry) {
        TypeHandlerLibrary library = new TypeHandlerLibraryImpl(moduleManager, typeRegistry);

        populateWithDefaultHandlers(library);

        return library;
    }

    private static void populateWithDefaultHandlers(TypeHandlerLibrary serializationLibrary) {
        // Current Supported
        serializationLibrary.addTypeHandlerFactory(new AssetTypeHandlerFactory());

        serializationLibrary.addTypeHandler(Name.class, new NameTypeHandler());
        serializationLibrary.addTypeHandler(TextureRegion.class, new TextureRegionTypeHandler());
        serializationLibrary.addTypeHandler(UITextureRegion.class, new UITextureRegionTypeHandler());

        serializationLibrary.addTypeHandlerFactory(new TextureRegionAssetTypeHandlerFactory());

        serializationLibrary.addTypeHandler(Color.class, new ColorTypeHandler());
        serializationLibrary.addTypeHandler(Colorc.class, new ColorcTypeHandler());

        serializationLibrary.addTypeHandler(org.joml.Vector4f.class, new Vector4fTypeHandler());
        serializationLibrary.addTypeHandler(Vector4fc.class, new Vector4fcTypeHandler());

        serializationLibrary.addTypeHandler(org.joml.Vector3f.class, new Vector3fTypeHandler());
        serializationLibrary.addTypeHandler(Vector3fc.class, new Vector3fcTypeHandler());

        serializationLibrary.addTypeHandler(org.joml.Vector2f.class, new Vector2fTypeHandler());
        serializationLibrary.addTypeHandler(Vector2fc.class, new Vector2fcTypeHandler());

        serializationLibrary.addTypeHandler(org.joml.Vector3i.class, new Vector3iTypeHandler());
        serializationLibrary.addTypeHandler(Vector3ic.class, new Vector3icTypeHandler());

        serializationLibrary.addTypeHandler(org.joml.Vector2i.class, new Vector2iTypeHandler());
        serializationLibrary.addTypeHandler(Vector2ic.class, new Vector2icTypeHandler());

        serializationLibrary.addTypeHandler(org.joml.Vector4i.class, new Vector4iTypeHandler());
        serializationLibrary.addTypeHandler(Vector4ic.class, new Vector4icTypeHandler());

        serializationLibrary.addTypeHandler(Rectanglei.class, new RectangleiTypeHandler());
        serializationLibrary.addTypeHandler(Rectanglef.class, new RectanglefTypeHandler());

        serializationLibrary.addTypeHandler(AABBi.class, new AABBiTypeHandler());
        serializationLibrary.addTypeHandler(AABBf.class, new AABBfTypeHandler());
        serializationLibrary.addTypeHandler(BlockRegion.class, new BlockRegionTypeHandler());
        serializationLibrary.addTypeHandler(BlockArea.class, new BlockAreaTypeHandler());
        serializationLibrary.addTypeHandler(BlockAreac.class, new BlockAreacTypeHandler());

        serializationLibrary.addTypeHandler(Quaternionf.class, new QuaternionfTypeHandler());
        serializationLibrary.addTypeHandler(Quaternionfc.class, new QuaternionfcTypeHandler());

        serializationLibrary.addTypeHandler(Prefab.class, new PrefabTypeHandler());
        serializationLibrary.addTypeHandler(IntegerRange.class, new IntegerRangeHandler());
    }

}
