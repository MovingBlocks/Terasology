// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.identity.storageServiceClient;

import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Base64;

/**
 * Alternative serializer for BigIntegers, which encodes them as base64 strings instead of the ASCII representation
 * of the number, This saves bandwidth during transfers of identity certificates.
 */
public class BigIntegerBase64Serializer implements JsonSerializer<BigInteger>, JsonDeserializer<BigInteger> {

    private static final Base64.Decoder DECODER = Base64.getDecoder();
    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final BigIntegerBase64Serializer INSTANCE = new BigIntegerBase64Serializer();

    public static BigIntegerBase64Serializer getInstance() {
        return INSTANCE;
    }

    @Override
    public BigInteger deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new BigInteger(DECODER.decode(json.getAsString().replace("\n", "")));
    }

    @Override
    public JsonElement serialize(BigInteger src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(ENCODER.encodeToString(src.toByteArray()));
    }
}
