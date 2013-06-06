/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.rendering.assetLoaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.vecmath.Point4i;
import javax.vecmath.Tuple4i;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.rendering.assets.Font;
import org.terasology.rendering.assets.FontCharacter;
import org.terasology.rendering.assets.Texture;

import com.google.common.collect.Lists;

/**
 * @author Immortius
 */
public class FontLoader implements AssetLoader<Font> {

    private static final Logger logger = LoggerFactory.getLogger(FontLoader.class);

    private final static String INTEGER_PATTERN = "((?:[\\+-]?\\d+)(?:[eE][\\+-]?\\d+)?)";

    // Header patterns
    private Pattern facePattern = Pattern.compile("face=\"(.*)\"");
    private Pattern sizePattern = Pattern.compile("size=" + INTEGER_PATTERN);
    private Pattern boldPattern = Pattern.compile("bold=" + INTEGER_PATTERN);
    private Pattern italicPattern = Pattern.compile("italic=" + INTEGER_PATTERN);
    private Pattern charsetPatten = Pattern.compile("charset=\"(.*)\"");
    private Pattern unicodePatten = Pattern.compile("unicode=" + INTEGER_PATTERN);
    private Pattern stretchHPattern = Pattern.compile("stretchH=" + INTEGER_PATTERN);
    private Pattern smoothPattern = Pattern.compile("smooth=" + INTEGER_PATTERN);
    private Pattern aaPattern = Pattern.compile("aa=" + INTEGER_PATTERN);
    private Pattern paddingPattern = Pattern.compile("padding=" + INTEGER_PATTERN + "," + INTEGER_PATTERN + "," + INTEGER_PATTERN + "," + INTEGER_PATTERN);
    private Pattern spacingPattern = Pattern.compile("spacing=" + INTEGER_PATTERN + "," + INTEGER_PATTERN);
    private Pattern outlinePattern = Pattern.compile("outline=" + INTEGER_PATTERN);

    // Common patterns
    private Pattern lineHeightPattern = Pattern.compile("lineHeight=" + INTEGER_PATTERN);
    private Pattern basePattern = Pattern.compile("base=" + INTEGER_PATTERN);
    private Pattern scaleWPattern = Pattern.compile("scaleW=" + INTEGER_PATTERN);
    private Pattern scaleHPattern = Pattern.compile("scaleH=" + INTEGER_PATTERN);
    private Pattern pagesPattern = Pattern.compile("pages=" + INTEGER_PATTERN);
    private Pattern packedPattern = Pattern.compile("packed=" + INTEGER_PATTERN);
    private Pattern alphaChannelPattern = Pattern.compile("alphaChnl=" + INTEGER_PATTERN);
    private Pattern redChannelPattern = Pattern.compile("redChnl=" + INTEGER_PATTERN);
    private Pattern greenChannelPattern = Pattern.compile("greenChnl=" + INTEGER_PATTERN);
    private Pattern blueChannelPattern = Pattern.compile("blueChnl=" + INTEGER_PATTERN);

    private Pattern pagePattern = Pattern.compile("page id=" + INTEGER_PATTERN + " file=\"(.*)\"");
    private Pattern charsPattern = Pattern.compile("chars count=" + INTEGER_PATTERN);
    private Pattern charPattern = Pattern.compile("char\\s+" +
            "id=" + INTEGER_PATTERN + "\\s+" +
            "x=" + INTEGER_PATTERN + "\\s+" +
            "y=" + INTEGER_PATTERN + "\\s+" +
            "width=" + INTEGER_PATTERN + "\\s+" +
            "height=" + INTEGER_PATTERN + "\\s+" +
            "xoffset=" + INTEGER_PATTERN + "\\s+" +
            "yoffset=" + INTEGER_PATTERN + "\\s+" +
            "xadvance=" + INTEGER_PATTERN + "\\s+" +
            "page=" + INTEGER_PATTERN + "\\s+" +
            "chnl=" + INTEGER_PATTERN + "\\s*");

    @Override
    public Font load(AssetUri uri, InputStream stream, List<URL> urls) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        FontHeader header = parseHeader(uri, reader.readLine());
        FontCommon common = parseCommon(uri, reader.readLine());

        List<FontPage> pages = Lists.newArrayList();
        for (int i = 0; i < common.pages; ++i) {
            pages.add(parsePage(uri, reader.readLine()));
        }

        int charCount = getCharacterCount(uri, reader.readLine());
        List<FontChar> characters = Lists.newArrayListWithCapacity(charCount);
        for (int i = 0; i < charCount; ++i) {
            characters.add(parseCharacter(uri, reader.readLine()));
        }

        List<Texture> textures = Lists.newArrayListWithCapacity(common.pages);
        for (FontPage page : pages) {
            textures.add(Assets.getTexture(uri.getPackage(), page.textureFile.substring(0, page.textureFile.lastIndexOf('.'))));
        }

        Font result = new Font(uri);
        result.setLineHeight(common.lineHeight);

        for (FontChar character : characters) {
            Texture tex = textures.get(character.page);
            FontCharacter finalChar = new FontCharacter(
                    ((float) character.x) / tex.getWidth(),
                    ((float) character.y) / tex.getHeight(),
                    character.width,
                    character.height,
                    character.xOffset,
                    character.yOffset,
                    character.xAdvance,
                    tex);
            result.setCharacter(character.id, finalChar);
        }

        return result;
    }

    private FontChar parseCharacter(AssetUri uri, String charInfo) throws IOException {
        Matcher matcher = charPattern.matcher(charInfo);
        if (matcher.matches()) {
            FontChar result = new FontChar();
            result.id = Integer.parseInt(matcher.group(1));
            result.x = Integer.parseInt(matcher.group(2));
            result.y = Integer.parseInt(matcher.group(3));
            result.width = Integer.parseInt(matcher.group(4));
            result.height = Integer.parseInt(matcher.group(5));
            result.xOffset = Integer.parseInt(matcher.group(6));
            result.yOffset = Integer.parseInt(matcher.group(7));
            result.xAdvance = Integer.parseInt(matcher.group(8));
            result.page = Integer.parseInt(matcher.group(9));
            result.channel = Integer.parseInt(matcher.group(10));
            return result;
        } else {
            throw new IOException("Failed to parse '" + uri + "' - invalid char line '" + charInfo + "'");
        }
    }

    private int getCharacterCount(AssetUri uri, String charsInfo) throws IOException {
        Matcher charsMatcher = charsPattern.matcher(charsInfo);
        if (charsMatcher.matches()) {
            return Integer.parseInt(charsMatcher.group(1));
        } else {
            throw new IOException("Failed to load '" + uri + "' - invalid chars line '" + charsInfo + "'");
        }
    }

    private FontPage parsePage(AssetUri uri, String pageInfo) throws IOException {
        Matcher pageMatcher = pagePattern.matcher(pageInfo);
        if (pageMatcher.matches()) {
            FontPage page = new FontPage();
            page.id = Integer.parseInt(pageMatcher.group(1));
            page.textureFile = pageMatcher.group(2);
            return page;
        } else {
            throw new IOException("Failed to load '" + uri + "' - invalid page line '" + pageInfo + "'");
        }
    }


    private FontHeader parseHeader(AssetUri uri, String info) throws IOException {
        if (!info.startsWith("info ")) {
            throw new IOException("Failed to load '" + uri + "' - missing info line");
        }
        FontHeader header = new FontHeader();
        header.face = findString(facePattern, info);
        header.size = findInteger(sizePattern, info);
        header.bold = findBool(boldPattern, info);
        header.italic = findBool(italicPattern, info);
        header.charset = findString(charsetPatten, info);
        header.unicode = findBool(unicodePatten, info);
        header.stretchH = findInteger(stretchHPattern, info);
        header.smooth = findBool(smoothPattern, info);
        header.aa = findBool(aaPattern, info);
        header.outline = findBool(outlinePattern, info);

        Matcher paddingMatcher = paddingPattern.matcher(info);
        if (paddingMatcher.find()) {
            header.padding.set(Integer.parseInt(paddingMatcher.group(1)), Integer.parseInt(paddingMatcher.group(2)), Integer.parseInt(paddingMatcher.group(3)), Integer.parseInt(paddingMatcher.group(4)));
        }

        Matcher spacingMatcher = spacingPattern.matcher(info);
        if (spacingMatcher.find()) {
            header.spacingX = Integer.parseInt(spacingMatcher.group(1));
            header.spacingY = Integer.parseInt(spacingMatcher.group(2));
        }

        return header;
    }

    private FontCommon parseCommon(AssetUri uri, String commonLine) throws IOException {
        if (!commonLine.startsWith("common ")) {
            throw new IOException("Failed to load '" + uri + "' - missing common line");
        }
        FontCommon common = new FontCommon();
        common.lineHeight = findInteger(lineHeightPattern, commonLine);
        common.base = findInteger(basePattern, commonLine);
        common.scaleW = findInteger(scaleWPattern, commonLine);
        common.scaleH = findInteger(scaleHPattern, commonLine);
        common.pages = findInteger(pagesPattern, commonLine);
        common.packed = findBool(packedPattern, commonLine);
        common.alphaChannel = findInteger(alphaChannelPattern, commonLine);
        common.redChannel = findInteger(redChannelPattern, commonLine);
        common.greenChannel = findInteger(greenChannelPattern, commonLine);
        common.blueChannel = findInteger(blueChannelPattern, commonLine);
        return common;
    }

    private String findString(Pattern pattern, String in) {
        Matcher matcher = pattern.matcher(in);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private int findInteger(Pattern pattern, String in) {
        Matcher matcher = pattern.matcher(in);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    private boolean findBool(Pattern pattern, String in) {
        return findInteger(pattern, in) != 0;
    }

    private static class FontHeader {
        public String face;
        public int size;
        public boolean bold;
        public boolean italic;
        public String charset;
        public boolean unicode;
        public int stretchH;
        public boolean smooth;
        public boolean aa;
        public Tuple4i padding = new Point4i();
        public int spacingX;
        public int spacingY;
        public boolean outline;
    }

    private static class FontCommon {
        public int lineHeight;
        public int base;
        public int scaleW;
        public int scaleH;
        public int pages;
        public boolean packed;
        public int alphaChannel;
        public int redChannel;
        public int greenChannel;
        public int blueChannel;
    }

    private static class FontPage {
        public int id;
        public String textureFile;
    }

    private static class FontChar {
        public int id;
        public int x;
        public int y;
        public int width;
        public int height;
        public int xOffset;
        public int yOffset;
        public int xAdvance;
        public int page;
        public int channel;
    }
}
