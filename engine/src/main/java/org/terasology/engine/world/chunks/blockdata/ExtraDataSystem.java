// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.blockdata;

import org.terasology.context.annotation.API;
import org.terasology.context.annotation.Index;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for classes which register extra per-block data fields.
 * Methods for this purpose must also be annotated with {@link RegisterExtraData}.
 */
@API
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Index
public @interface ExtraDataSystem {
}
