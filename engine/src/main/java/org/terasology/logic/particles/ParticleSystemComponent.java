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
package org.terasology.logic.particles;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.engine.Uri;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.*;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.particles.BlockParticleEffectComponent.Particle;
import org.terasology.math.Vector3i;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.logic.NearestSortingList;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.WorldProvider;
import org.terasology.world.biomes.Biome;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.loader.WorldAtlas;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Linus van Elswijk
 */
public class ParticleSystemComponent implements Component {

    public final boolean oneTime;

    public final List<AffectorFunction> affectorFunctions;
    public final EmitterFunction emmiter;
    public final ParticlePool particlePool;

    // package private
    ParticleSystemComponent(
            final boolean oneTime,
            final List<AffectorFunction> affectorFunctions,
            final EmitterFunction emmiter,
            final ParticlePool particlePool
    ) {
        this.oneTime = oneTime;
        this.affectorFunctions = affectorFunctions;
        this.emmiter = emmiter;
        this.particlePool = particlePool;
    }
}
