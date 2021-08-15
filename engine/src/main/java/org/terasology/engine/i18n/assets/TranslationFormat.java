// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.i18n.assets;

import com.google.common.base.Charsets;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import org.terasology.engine.core.SimpleUri;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.exceptions.InvalidAssetFilenameException;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.format.AssetFileFormat;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.gestalt.module.resources.FileReference;
import org.terasology.gestalt.naming.Name;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines the file format of translation data. Matching files are of the form:
 * <pre>
 * &lt;project-name&gt;.lang
 * &lt;project-name&gt;_&lt;language-tag&gt;.lang
 * </pre>
 * All IETF BCP 47 language tags are supported, but consider sticking to ISO 639-1 for simplicity.
 * The data in the files is expected to be JSON entries of the form of a &lt;String, String&gt; map.
 */
@RegisterAssetFileFormat
public class TranslationFormat implements AssetFileFormat<TranslationData> {

    /**
     * The extension of translation files
     */
    public static final String LANGDATA_EXT = ".lang";

    private static final Pattern FILENAME_PATTERN = Pattern.compile("([^_]+)_?([\\w-]+)?.lang");


    private static final TypeToken<Map<String, String>> MAP_TOKEN = new TypeToken<Map<String, String>>() {
        private static final long serialVersionUID = -2255189133660408141L;
    };

    private final JsonDeserializer<String> stringArraySerializer = (json, typeOfSrc, context) -> {
        if (json.isJsonArray()) {
            StringBuilder stringFromArray = new StringBuilder();
            json.getAsJsonArray().forEach(o -> stringFromArray.append(o.getAsString()));
            return stringFromArray.toString();
        } else {
            return json.getAsString();
        }
    };

    private final Gson gson = new GsonBuilder().registerTypeAdapter(String.class, stringArraySerializer).create();

    @Override
    public Predicate<FileReference> getFileMatcher() {
        return path -> {
            String name = path.getName();
            return FILENAME_PATTERN.matcher(name).matches();
        };
    }

    @Override
    public Name getAssetName(String filename) throws InvalidAssetFilenameException {
        if (filename.endsWith(LANGDATA_EXT)) {
            return new Name(filename.substring(0, filename.length() - LANGDATA_EXT.length()));
        }
        throw new InvalidAssetFilenameException("File '" + filename + "' does not end with '" + LANGDATA_EXT + "'.");
    }

    @Override
    public TranslationData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {

        if (inputs.size() != 1) {
            throw new IOException("Failed to load translation data '" + urn + "': " + inputs);
        }

        AssetDataFile file = inputs.get(0);

        Locale locale = localeFromFilename(file.getFilename());
        Name projName = basenameFromFilename(file.getFilename());

        SimpleUri projUri = new SimpleUri(urn.getModuleName(), projName);
        TranslationData data = new TranslationData(projUri, locale);

        try (InputStreamReader isr = new InputStreamReader(file.openStream(), Charsets.UTF_8)) {
            Map<String, String> entry = gson.fromJson(isr, MAP_TOKEN.getType());
            data.addAll(entry);
        } catch (JsonParseException e) {
            throw new IOException("Could not parse file '" + file + "'", e);
        }

        return data;
    }

    private static Name basenameFromFilename(String filename) throws IOException {
        Matcher m = FILENAME_PATTERN.matcher(filename);
        if (m.matches()) {
            return new Name(m.group(1));
        }
        throw new IOException("Could not parse project name: " + filename);
    }

    private static Locale localeFromFilename(String filename) throws IOException {
        Matcher m = FILENAME_PATTERN.matcher(filename);
        if (m.matches()) {
            String langTag = m.group(2);
            return (langTag != null) ? Locale.forLanguageTag(langTag) : Locale.ROOT;
        }
        throw new IOException("Could not parse locale: " + filename);
    }
}
