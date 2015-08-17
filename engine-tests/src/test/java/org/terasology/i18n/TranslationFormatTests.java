/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.i18n;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.exceptions.InvalidAssetFilenameException;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.format.FileFormat;
import org.terasology.engine.SimpleUri;
import org.terasology.i18n.assets.TranslationData;
import org.terasology.i18n.assets.TranslationFormat;
import org.terasology.naming.Name;

/**
 * Tests for the {@link TranslationFormat} class.
 */
public class TranslationFormatTests {

    private TranslationFormat format;

    @Before
    public void setup() {
        format = new TranslationFormat();
    }

    @Test
    public void testGetAssetName() throws InvalidAssetFilenameException {
        Assert.assertEquals(new Name("menu"),  format.getAssetName("menu.lang"));
        Assert.assertEquals(new Name("menu_pl"),  format.getAssetName("menu_pl.lang"));
    }

    @Test
    public void testPathMatcher() {
        Assert.assertFalse(format.getFileMatcher().matches(Paths.get("menu.json")));
        Assert.assertFalse(format.getFileMatcher().matches(Paths.get("menu.prefab")));

        Assert.assertTrue(format.getFileMatcher().matches(Paths.get("menu.lang")));
        Assert.assertTrue(format.getFileMatcher().matches(Paths.get("menu_pl.lang")));
        Assert.assertTrue(format.getFileMatcher().matches(Paths.get("menu_en-US-x-lvariant-POSIX.lang")));
    }

    @Test
    public void testEmptyDataGenRoot() throws IOException, InvalidAssetFilenameException {
        AssetDataFile assetDataFile = mockAssetDataFile("menu.lang", "{}".getBytes(StandardCharsets.UTF_8));
        ResourceUrn urn = createUrnFromFile(format, assetDataFile);

        TranslationData data = format.load(urn, Collections.singletonList(assetDataFile));
        Assert.assertEquals(new SimpleUri("engine:menu"), data.getProjectUri());
        Assert.assertEquals(Locale.ROOT, data.getLocale());
    }

    @Test
    public void testEmptyDataGenGermany() throws IOException, InvalidAssetFilenameException {
        AssetDataFile assetDataFile = mockAssetDataFile("menu_de-DE.lang", "{}".getBytes(StandardCharsets.UTF_8));
        ResourceUrn urn = createUrnFromFile(format, assetDataFile);

        TranslationData data = format.load(urn, Collections.singletonList(assetDataFile));
        Assert.assertEquals(Locale.GERMANY, data.getLocale());
        Assert.assertTrue(data.getTranslations().isEmpty());
    }

    @Test
    public void testDataGenGerman() throws IOException, InvalidAssetFilenameException {
        byte[] resource = createSimpleTranslationFile().getBytes(StandardCharsets.UTF_8);
        AssetDataFile assetDataFile = mockAssetDataFile("menu_de-DE.lang", resource);
        ResourceUrn urn = createUrnFromFile(format, assetDataFile);

        TranslationData data = format.load(urn, Collections.singletonList(assetDataFile));
        Assert.assertEquals("Einzelspieler", data.getTranslations().get("engine:mainMenuScreen#singleplayer#text"));
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

    private static String createSimpleTranslationFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"engine:mainMenuScreen#singleplayer#text\": \"Einzelspieler\",");
        sb.append("\"engine:mainMenuScreen#exit#text\": \"Beenden\"");
        sb.append("}");
        return sb.toString();
    }
}
