/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License"FF);
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

package org.terasology.rendering.nui.layers.mainMenu.settings;

import org.terasology.rendering.nui.Color;

import com.google.common.collect.ImmutableList;


/**
 * Viewing conditions are modeled after sRGB's "typical" viewing environment with 200 cd/m2.
 * Consecutive colors have a delta E distance of at least 1.
 * Delta E distance is defined in CAM02-UCS as published in "Uniform Colour Spaces Based on
 * CIECAM02 Colour Appearance Model" (Luo et al.)
 */
public final class CieCamColors {

    /**
     * Luminance (in CIE-Lch) is 65 for all color tones, Chroma is at 65.
     * The entire hue circle is sampled (non-linearly).
     * The color plane is transformed with some clipping (mostly blue and red) into RGB.
     */
    public static final ImmutableList<Color> L65C65 = ImmutableList.of(
            new Color(0xFF77AEFF), new Color(0xFF77ABFF), new Color(0xFF77A8FF), new Color(0xFF77A5FF),
            new Color(0xFF78A2FF), new Color(0xFF789FFF), new Color(0xFF789BFF), new Color(0xFF7898FF),
            new Color(0xFF7895FF), new Color(0xFF7992FF), new Color(0xFF798FFF), new Color(0xFF798CFF),
            new Color(0xFF7989FF), new Color(0xFF7A86FF), new Color(0xFF7A82FF), new Color(0xFF7B7FFF),
            new Color(0xFF7B7CFF), new Color(0xFF7B79FF), new Color(0xFF7C76FF), new Color(0xFF7D73FF),
            new Color(0xFF7D6FFF), new Color(0xFF7E6CFF), new Color(0xFF7E69FF), new Color(0xFF7F66FF),
            new Color(0xFF8062FF), new Color(0xFF815FFF), new Color(0xFF815CFF), new Color(0xFF8259FF),
            new Color(0xFF8356FF), new Color(0xFF8453FF), new Color(0xFF8550FF), new Color(0xFF864DFF),
            new Color(0xFF874AFF), new Color(0xFF8846FF), new Color(0xFF8943FF), new Color(0xFF8A40FF),
            new Color(0xFF8B3DFF), new Color(0xFF8C3AFF), new Color(0xFF8D37FF), new Color(0xFF8E34FF),
            new Color(0xFF8F31FF), new Color(0xFF902EFF), new Color(0xFF912BFF), new Color(0xFF9228FF),
            new Color(0xFE9425FF), new Color(0xFD9522FF), new Color(0xFB961EFF), new Color(0xFA971BFF),
            new Color(0xF89817FF), new Color(0xF79A13FF), new Color(0xF59B0FFF), new Color(0xF39C0AFF),
            new Color(0xF29D05FF), new Color(0xF09F01FF), new Color(0xEEA000FF), new Color(0xECA100FF),
            new Color(0xEAA200FF), new Color(0xE8A300FF), new Color(0xE6A500FF), new Color(0xE4A600FF),
            new Color(0xE2A700FF), new Color(0xE0A800FF), new Color(0xDDAA00FF), new Color(0xDBAB00FF),
            new Color(0xD9AC00FF), new Color(0xD6AD00FF), new Color(0xD4AF00FF), new Color(0xD2B000FF),
            new Color(0xCFB100FF), new Color(0xCCB200FF), new Color(0xCAB300FF), new Color(0xC7B500FF),
            new Color(0xC4B600FF), new Color(0xC2B700FF), new Color(0xBFB804FF), new Color(0xBCB90AFF),
            new Color(0xB9BA0FFF), new Color(0xB6BB13FF), new Color(0xB3BC17FF), new Color(0xB0BE1BFF),
            new Color(0xADBF1FFF), new Color(0xA9C022FF), new Color(0xA6C125FF), new Color(0xA3C229FF),
            new Color(0x9FC32CFF), new Color(0x9CC42FFF), new Color(0x98C532FF), new Color(0x94C535FF),
            new Color(0x91C638FF), new Color(0x8DC73BFF), new Color(0x89C83FFF), new Color(0x85C942FF),
            new Color(0x81CA45FF), new Color(0x7DCB48FF), new Color(0x78CB4BFF), new Color(0x74CC4EFF),
            new Color(0x6FCD51FF), new Color(0x6BCE54FF), new Color(0x66CE57FF), new Color(0x60CF5AFF),
            new Color(0x5BD05DFF), new Color(0x55D060FF), new Color(0x4FD163FF), new Color(0x49D266FF),
            new Color(0x41D269FF), new Color(0x39D36CFF), new Color(0x30D36FFF), new Color(0x25D472FF),
            new Color(0x15D475FF), new Color(0x00D578FF), new Color(0x00D57BFF), new Color(0x00D57EFF),
            new Color(0x00D681FF), new Color(0x00D684FF), new Color(0x00D787FF), new Color(0x00D78AFF),
            new Color(0x00D78DFF), new Color(0x00D790FF), new Color(0x00D893FF), new Color(0x00D896FF),
            new Color(0x00D899FF), new Color(0x00D89CFF), new Color(0x00D89FFF), new Color(0x00D8A2FF),
            new Color(0x00D8A5FF), new Color(0x00D9A8FF), new Color(0x00D9ABFF), new Color(0x00D9AEFF),
            new Color(0x00D9B1FF), new Color(0x00D8B4FF), new Color(0x00D8B7FF), new Color(0x00D8BAFF),
            new Color(0x00D8BDFF), new Color(0x00D8C0FF), new Color(0x00D8C3FF), new Color(0x00D8C6FF),
            new Color(0x00D7C9FF), new Color(0x00D7CBFF), new Color(0x00D7CEFF), new Color(0x00D7D1FF),
            new Color(0x00D6D4FF), new Color(0x00D6D7FF), new Color(0x00D5DAFF), new Color(0x00D5DDFF),
            new Color(0x00D5DFFF), new Color(0x00D4E2FF), new Color(0x00D4E5FF), new Color(0x00D3E8FF),
            new Color(0x00D2EBFF), new Color(0x00D2EDFF), new Color(0x00D1F0FF), new Color(0x00D0F3FF),
            new Color(0x00D0F5FF), new Color(0x00CFF8FF), new Color(0x00CEFBFF), new Color(0x00CDFDFF),
            new Color(0x00CDFFFF), new Color(0x00CCFFFF), new Color(0x00CBFFFF), new Color(0x00CAFFFF),
            new Color(0x00C9FFFF), new Color(0x00C8FFFF), new Color(0x00C7FFFF), new Color(0x12C6FFFF),
            new Color(0x25C5FFFF), new Color(0x32C3FFFF), new Color(0x3CC2FFFF), new Color(0x45C1FFFF),
            new Color(0x4DC0FFFF), new Color(0x55BFFFFF), new Color(0x5BBDFFFF), new Color(0x62BCFFFF),
            new Color(0x68BBFFFF), new Color(0x6EB9FFFF), new Color(0x73B8FFFF), new Color(0x78B6FFFF),
            new Color(0x7DB5FFFF), new Color(0x82B4FFFF), new Color(0x87B2FFFF), new Color(0x8BB1FFFF),
            new Color(0x90AFFFFF), new Color(0x94AEFFFF), new Color(0x98ACFFFF), new Color(0x9CABFFFF),
            new Color(0xA0A9FFFF), new Color(0xA4A8FFFF), new Color(0xA8A6FFFF), new Color(0xACA5FFFF),
            new Color(0xAFA4FFFF), new Color(0xB2A2FFFF), new Color(0xB6A1FFFF), new Color(0xB99FFFFF),
            new Color(0xBC9EFFFF), new Color(0xBF9DFFFF), new Color(0xC29BFFFF), new Color(0xC59AFFFF),
            new Color(0xC898FFFF), new Color(0xCB97FFFF), new Color(0xCD96FFFF), new Color(0xD094FFFF),
            new Color(0xD393FFFF), new Color(0xD592FFFF), new Color(0xD791FFFF), new Color(0xDA90FFFF),
            new Color(0xDC8EFFFF), new Color(0xDE8DFFFF), new Color(0xE18CFFFF), new Color(0xE38BFFFF),
            new Color(0xE58AFFFF), new Color(0xE689FFFF), new Color(0xE888FFFF), new Color(0xEA87FFFF),
            new Color(0xEC86FFFF), new Color(0xEE85FFFF), new Color(0xF084FFFF), new Color(0xF183FFFF),
            new Color(0xF383FFFF), new Color(0xF582FCFF), new Color(0xF681FAFF), new Color(0xF880F7FF),
            new Color(0xF980F5FF), new Color(0xFA7FF2FF), new Color(0xFC7EEFFF), new Color(0xFD7EEDFF),
            new Color(0xFE7DEAFF), new Color(0xFF7DE7FF), new Color(0xFF7CE4FF), new Color(0xFF7BE1FF),
            new Color(0xFF7BDEFF), new Color(0xFF7BDBFF), new Color(0xFF7AD8FF), new Color(0xFF7AD5FF),
            new Color(0xFF79D2FF), new Color(0xFF79CFFF), new Color(0xFF79CCFF), new Color(0xFF78C9FF),
            new Color(0xFF78C6FF), new Color(0xFF78C2FF), new Color(0xFF78BFFF), new Color(0xFF78BCFF),
            new Color(0xFF78B9FF), new Color(0xFF77B5FF));


    private CieCamColors() {
        // no instances
    }
}
