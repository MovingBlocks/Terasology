// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.i18n;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.i18n.assets.TranslationData;
import org.terasology.engine.i18n.assets.TranslationFormat;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.exceptions.InvalidAssetFilenameException;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.format.FileFormat;
import org.terasology.gestalt.module.resources.FileReference;
import org.terasology.gestalt.naming.Name;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link TranslationFormat} class.
 */
public class TranslationFormatTests {

    private TranslationFormat format;

    @BeforeEach
    public void setup() {
        format = new TranslationFormat();
    }

    @Test
    public void testGetAssetName()  throws InvalidAssetFilenameException {
        assertEquals(new Name("menu"),  format.getAssetName("menu.lang"));
        assertEquals(new Name("menu_pl"),  format.getAssetName("menu_pl.lang"));
    }

    @Test
    public void testPathMatcher() {
        Predicate<FileReference> matcher = format.getFileMatcher();
        assertFalse(matcher.test(new CarefreeFileReference("menu.json")));
        assertFalse(matcher.test(new CarefreeFileReference("menu.prefab")));

        assertTrue(matcher.test(new CarefreeFileReference("menu.lang")));
        assertTrue(matcher.test(new CarefreeFileReference("menu_pl.lang")));
        assertTrue(matcher.test(new CarefreeFileReference("menu_en-US-x-lvariant-POSIX.lang")));
    }

    @Test
    public void testEmptyDataGenRoot() throws IOException, InvalidAssetFilenameException {
        AssetDataFile assetDataFile = mockAssetDataFile("menu.lang", "{}".getBytes(StandardCharsets.UTF_8));
        ResourceUrn urn = createUrnFromFile(format, assetDataFile);

        TranslationData data = format.load(urn, Collections.singletonList(assetDataFile));
        assertEquals(new SimpleUri("engine:menu"), data.getProjectUri());
        assertEquals(Locale.ROOT, data.getLocale());
    }

    @Test
    public void testEmptyDataGenGermany() throws IOException, InvalidAssetFilenameException {
        AssetDataFile assetDataFile = mockAssetDataFile("menu_de-DE.lang", "{}".getBytes(StandardCharsets.UTF_8));
        ResourceUrn urn = createUrnFromFile(format, assetDataFile);

        TranslationData data = format.load(urn, Collections.singletonList(assetDataFile));
        assertEquals(Locale.GERMANY, data.getLocale());
        assertTrue(data.getTranslations().isEmpty());
    }

    @Test
    public void testDataGenGerman() throws IOException, InvalidAssetFilenameException {
        byte[] resource = createSimpleTranslationFile().getBytes(StandardCharsets.UTF_8);
        AssetDataFile assetDataFile = mockAssetDataFile("menu_de-DE.lang", resource);
        ResourceUrn urn = createUrnFromFile(format, assetDataFile);

        TranslationData data = format.load(urn, Collections.singletonList(assetDataFile));
        assertEquals("Einzelspieler", data.getTranslations().get("engine:mainMenuScreen#singleplayer#text"));
    }

    @Test
    public void testMultiLine() throws IOException, InvalidAssetFilenameException {
        byte[] resource = createSimpleMultiLineTranslationFile().getBytes(StandardCharsets.UTF_8);
        AssetDataFile assetDataFile = mockAssetDataFile("game.lang", resource);
        ResourceUrn urn = createUrnFromFile(format, assetDataFile);

        TranslationData data = format.load(urn, Collections.singletonList(assetDataFile));
        assertEquals("line 1 \n line 2 \n line 3", data.getTranslations().get("multi-line"));
        assertEquals("line 1 \n line 2 \n line 3", data.getTranslations().get("single-line"));
    }

    // TODO: consider making this available to other test classes
    private static AssetDataFile mockAssetDataFile(String fname, byte[] resource) throws IOException {
        AssetDataFile assetDataFile = mock(AssetDataFile.class);
        when(assetDataFile.openStream()).thenReturn(new BufferedInputStream(new ByteArrayInputStream(resource)));
        when(assetDataFile.getFilename()).thenReturn(fname);
        return assetDataFile;
    }

    private static ResourceUrn createUrnFromFile(FileFormat format, AssetDataFile file) throws InvalidAssetFilenameException {
        Name assetName = format.getAssetName(file.getFilename());
        return new ResourceUrn("engine:" + assetName.toString());
    }

    private static String createSimpleMultiLineTranslationFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"multi-line\": [\"line 1 \n \",\"line 2 \n \",\"line 3\"],");
        sb.append("\"single-line\": \"line 1 \n line 2 \n line 3\"");
        sb.append("}");
        return sb.toString();
    }


    private static String createSimpleTranslationFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"engine:mainMenuScreen#singleplayer#text\": \"Einzelspieler\",");
        sb.append("\"engine:mainMenuScreen#exit#text\": \"Beenden\"");
        sb.append("}");
        return sb.toString();
    }


    static class CarefreeFileReference implements FileReference {
        private final String name;

        CarefreeFileReference(String filename) {
            name = filename;
        }
        
        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<String> getPath() {
            return ImmutableList.of(name);
        }

        @Override
        public InputStream open() throws IOException {
            throw new IOException("I never expected to have to open a file!");
        }
    }
}
