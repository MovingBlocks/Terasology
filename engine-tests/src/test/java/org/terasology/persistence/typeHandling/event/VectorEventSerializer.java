// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.event;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.module.DependencyResolver;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ResolutionResult;
import org.terasology.persistence.serializers.EventSerializer;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.protobuf.EntityData;
import org.terasology.reflection.TypeRegistry;
import org.terasology.testUtil.ModuleManagerFactory;
import org.terasology.testUtil.TeraAssert;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class VectorEventSerializer extends TerasologyTestingEnvironment {

    private TypeHandlerLibrary typeHandlerLibrary;
    private EventSerializer serializer;
    private ModuleManager moduleManager;
    private TypeRegistry typeRegistry;

    static class Vector3Constant implements Event {
        public Vector3fc v1;
        public Vector4fc v2;
        public Vector2fc v3;
    }

    @Override
    @BeforeEach
    public void setup() throws Exception {
        super.setup();

        final JavaArchive homeArchive = ShrinkWrap.create(JavaArchive.class);
        final FileSystem vfs = ShrinkWrapFileSystems.newFileSystem(homeArchive);
        PathManager.getInstance().useOverrideHomePath(vfs.getPath(""));

        moduleManager = ModuleManagerFactory.create();

        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        ResolutionResult result = resolver.resolve(moduleManager.getRegistry().getModuleIds());

        assumeTrue(result.isSuccess());

        ModuleEnvironment environment = moduleManager.loadEnvironment(result.getModules(), true);
        typeRegistry = new TypeRegistry(environment);


        typeHandlerLibrary = TypeHandlerLibrary.forModuleEnvironment(moduleManager, typeRegistry);

        this.serializer = new EventSerializer(context.get(EventLibrary.class), typeHandlerLibrary);
    }


    @Test
    public void testEventSerializationConstant() throws IOException {
        Vector3Constant a = new Vector3Constant();
        a.v1 = new org.joml.Vector3f(1.0f, 2.0f, 3.0f);
        a.v2 = new org.joml.Vector4f(1.0f, 2.0f, 3.0f, 5.0f);
        a.v3 = new org.joml.Vector2f(1.0f, 2.0f);

        EntityData.Event ev = serializer.serialize(a);
        Event dev = serializer.deserialize(ev);
        assumeTrue(dev instanceof Vector3Constant);
        TeraAssert.assertEquals(((Vector3Constant) dev).v1, new org.joml.Vector3f(1.0f, 2.0f, 3.0f), .00001f);
        TeraAssert.assertEquals(((Vector3Constant) dev).v2, new org.joml.Vector4f(1.0f, 2.0f, 3.0f, 5.0f), .00001f);
        TeraAssert.assertEquals(((Vector3Constant) dev).v3, new org.joml.Vector2f(1.0f, 2.0f), .00001f);
    }
}
