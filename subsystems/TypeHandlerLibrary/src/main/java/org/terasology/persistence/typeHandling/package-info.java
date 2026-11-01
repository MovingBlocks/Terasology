// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

/**
 * This package contains the interfaces and abstract classes for use when defining type handlers.
 * Type handlers provide the algorithms for serializing and deserializing types - this uses an implementation agnostic set of interfaces
 * so TypeHandlers can be used for different serialization techniques (Json, Protobuf, etc).
 */
@API
package org.terasology.persistence.typeHandling;

import org.terasology.context.annotation.API;
