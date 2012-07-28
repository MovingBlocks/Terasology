package org.terasology.logic.world.generator.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;

import org.terasology.logic.world.generator.core.ChunkGeneratorManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

/**
 * Read and write the ChunkGeneratorManager from/to a JSON file.
 * 
 * @author Mathias Kalb
 */
public class ChunkGeneratorJSONFormat {

	public static void write(ChunkGeneratorManager chunkGeneratorManager,
			BufferedWriter writer) throws IOException {
		// TODO newGson().toJson(chunkGeneratorManager, writer);
	}

	public static ChunkGeneratorManager read(BufferedReader reader)
			throws IOException {
		try {
			return null; // TODO newGson().fromJson(reader, ChunkGeneratorManager.class);
		} catch (JsonSyntaxException e) {
			throw new IOException("Failed to load chunkGeneratorManager", e);
		}
	}

	private static Gson newGson() {
		return new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(ChunkGeneratorManager.class,
						new ChunkGeneratorManagerHandler())
				.create();
	}

	private static class ChunkGeneratorManagerHandler implements
			JsonSerializer<ChunkGeneratorManager>,
			JsonDeserializer<ChunkGeneratorManager> {

		@Override
		public ChunkGeneratorManager deserialize(JsonElement json,
				Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			return null;
		}

		@Override
		public JsonElement serialize(ChunkGeneratorManager src, Type typeOfSrc,
				JsonSerializationContext context) {
			JsonObject result = new JsonObject();
			result.add("className", new JsonPrimitive(src.getClass().getName()));
			result.add("typeOfSrc", new JsonPrimitive(typeOfSrc.toString()));
			return result;
		}
	}

}
