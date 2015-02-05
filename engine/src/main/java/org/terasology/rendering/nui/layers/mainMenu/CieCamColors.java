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

package org.terasology.rendering.nui.layers.mainMenu;

import org.terasology.rendering.nui.Color;

import com.google.common.collect.ImmutableList;


/**
 * Viewing conditions are modeled after sRGB's "typical" viewing environment with 200 cd/m2.
 * Consecutive colors have a delta E distance of at least 1.
 * Delta E distance is defined in CAM02-UCS as published in "Uniform Colour Spaces Based on
 * CIECAM02 Colour Appearance Model" (Luo et al.)
 * @author Martin Steiger
 */
public final class CieCamColors {

    /**
     * Luminance (in CIE-Lch) is 60 for all color tones, Chroma is at 50.
     * The entire hue circle is sampled (non-linearly).
     * The color plane is transformed with only little clipping into RGB.
     */
    public static final ImmutableList<Color> L60C50 = ImmutableList.of(
            new Color(0xF07EA4FF), new Color(0xF07EA1FF), new Color(0xF17E9EFF), new Color(0xF17E9BFF),
            new Color(0xF27E99FF), new Color(0xF27E96FF), new Color(0xF27E93FF), new Color(0xF27E90FF),
            new Color(0xF37E8EFF), new Color(0xF37E8BFF), new Color(0xF37F88FF), new Color(0xF37F85FF),
            new Color(0xF37F82FF), new Color(0xF37F80FF), new Color(0xF3807DFF), new Color(0xF3807AFF),
            new Color(0xF38177FF), new Color(0xF38175FF), new Color(0xF38172FF), new Color(0xF2826FFF),
            new Color(0xF2826DFF), new Color(0xF2836AFF), new Color(0xF18467FF), new Color(0xF18465FF),
            new Color(0xF08562FF), new Color(0xF08660FF), new Color(0xEF865DFF), new Color(0xEE875BFF),
            new Color(0xEE8858FF), new Color(0xED8856FF), new Color(0xEC8954FF), new Color(0xEB8A51FF),
            new Color(0xEA8B4FFF), new Color(0xE98C4DFF), new Color(0xE88D4BFF), new Color(0xE78E48FF),
            new Color(0xE68F46FF), new Color(0xE59044FF), new Color(0xE39143FF), new Color(0xE29241FF),
            new Color(0xE1933FFF), new Color(0xDF943DFF), new Color(0xDE953CFF), new Color(0xDC963AFF),
            new Color(0xDB9739FF), new Color(0xD99838FF), new Color(0xD79937FF), new Color(0xD59A36FF),
            new Color(0xD39B35FF), new Color(0xD29C34FF), new Color(0xD09E33FF), new Color(0xCE9F33FF),
            new Color(0xCCA033FF), new Color(0xC9A132FF), new Color(0xC7A232FF), new Color(0xC5A333FF),
            new Color(0xC3A433FF), new Color(0xC0A533FF), new Color(0xBEA634FF), new Color(0xBCA735FF),
            new Color(0xB9A936FF), new Color(0xB7AA37FF), new Color(0xB4AB38FF), new Color(0xB2AC39FF),
            new Color(0xAFAD3AFF), new Color(0xACAE3CFF), new Color(0xAAAF3DFF), new Color(0xA7B03FFF),
            new Color(0xA4B041FF), new Color(0xA2B143FF), new Color(0x9FB244FF), new Color(0x9CB346FF),
            new Color(0x99B448FF), new Color(0x96B54AFF), new Color(0x93B64CFF), new Color(0x90B64FFF),
            new Color(0x8DB751FF), new Color(0x8AB853FF), new Color(0x87B955FF), new Color(0x84B957FF),
            new Color(0x81BA5AFF), new Color(0x7EBB5CFF), new Color(0x7BBB5EFF), new Color(0x77BC61FF),
            new Color(0x74BC63FF), new Color(0x71BD65FF), new Color(0x6EBE68FF), new Color(0x6ABE6AFF),
            new Color(0x67BF6CFF), new Color(0x64BF6FFF), new Color(0x60C071FF), new Color(0x5DC074FF),
            new Color(0x59C076FF), new Color(0x56C179FF), new Color(0x52C17BFF), new Color(0x4EC17EFF),
            new Color(0x4BC280FF), new Color(0x47C283FF), new Color(0x43C285FF), new Color(0x3FC288FF),
            new Color(0x3BC38AFF), new Color(0x37C38DFF), new Color(0x32C38FFF), new Color(0x2EC392FF),
            new Color(0x29C394FF), new Color(0x24C397FF), new Color(0x1FC499FF), new Color(0x19C49CFF),
            new Color(0x12C49EFF), new Color(0x09C4A1FF), new Color(0x00C4A3FF), new Color(0x00C4A6FF),
            new Color(0x00C3A8FF), new Color(0x00C3ABFF), new Color(0x00C3ADFF), new Color(0x00C3B0FF),
            new Color(0x00C3B2FF), new Color(0x00C3B5FF), new Color(0x00C3B7FF), new Color(0x00C2BAFF),
            new Color(0x00C2BCFF), new Color(0x00C2BEFF), new Color(0x00C1C1FF), new Color(0x00C1C3FF),
            new Color(0x00C1C6FF), new Color(0x00C0C8FF), new Color(0x07C0CAFF), new Color(0x11BFCDFF),
            new Color(0x18BFCFFF), new Color(0x1FBED1FF), new Color(0x25BED3FF), new Color(0x2ABDD6FF),
            new Color(0x30BDD8FF), new Color(0x35BCDAFF), new Color(0x39BBDCFF), new Color(0x3EBADEFF),
            new Color(0x43BAE0FF), new Color(0x47B9E2FF), new Color(0x4CB8E4FF), new Color(0x50B7E6FF),
            new Color(0x54B6E8FF), new Color(0x58B6E9FF), new Color(0x5CB5EBFF), new Color(0x60B4EDFF),
            new Color(0x64B3EEFF), new Color(0x68B2F0FF), new Color(0x6CB1F1FF), new Color(0x70B0F2FF),
            new Color(0x74AFF4FF), new Color(0x78AEF5FF), new Color(0x7BADF6FF), new Color(0x7FABF7FF),
            new Color(0x82AAF8FF), new Color(0x86A9F8FF), new Color(0x89A8F9FF), new Color(0x8DA7FAFF),
            new Color(0x90A6FAFF), new Color(0x93A5FAFF), new Color(0x96A3FBFF), new Color(0x9AA2FBFF),
            new Color(0x9DA1FBFF), new Color(0xA0A0FBFF), new Color(0xA39FFBFF), new Color(0xA69EFBFF),
            new Color(0xA89CFAFF), new Color(0xAB9BFAFF), new Color(0xAE9AFAFF), new Color(0xB099F9FF),
            new Color(0xB398F8FF), new Color(0xB597F7FF), new Color(0xB896F7FF), new Color(0xBA95F6FF),
            new Color(0xBD94F5FF), new Color(0xBF93F4FF), new Color(0xC192F2FF), new Color(0xC391F1FF),
            new Color(0xC590F0FF), new Color(0xC78FEEFF), new Color(0xC98EEDFF), new Color(0xCB8DEBFF),
            new Color(0xCD8CEAFF), new Color(0xCF8BE8FF), new Color(0xD18AE6FF), new Color(0xD28AE4FF),
            new Color(0xD489E3FF), new Color(0xD688E1FF), new Color(0xD787DFFF), new Color(0xD987DDFF),
            new Color(0xDA86DBFF), new Color(0xDC85D8FF), new Color(0xDD85D6FF), new Color(0xDE84D4FF),
            new Color(0xE083D2FF), new Color(0xE183D0FF), new Color(0xE282CDFF), new Color(0xE382CBFF),
            new Color(0xE481C9FF), new Color(0xE581C6FF), new Color(0xE681C4FF), new Color(0xE780C1FF),
            new Color(0xE880BFFF), new Color(0xE980BCFF), new Color(0xEA7FBAFF), new Color(0xEB7FB7FF),
            new Color(0xEC7FB4FF), new Color(0xEC7EB2FF), new Color(0xED7EAFFF), new Color(0xEE7EACFF),
            new Color(0xEE7EAAFF));

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
