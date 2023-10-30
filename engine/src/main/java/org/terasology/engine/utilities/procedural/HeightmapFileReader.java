// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities.procedural;

import com.google.common.base.Charsets;
import com.google.common.math.DoubleMath;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;

/**
 * Reads a heightmap encoded in a textfile
 *
 */
public final class HeightmapFileReader {

    private HeightmapFileReader() {
    }

    public static void convertFileToTexture() throws IOException {
        float[][] heightmap = readFile();

        double scaleFactor = 256 * 256 * 12.8;

//        Slick's PNGDecoder does not support 16 bit textures

//        BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_USHORT_GRAY);
//        DataBufferUShort buffer = (DataBufferUShort) image.getRaster().getDataBuffer();
//        scaleFactor *= 256.0f;

//        Slick's PNGDecoder does not support grayscale textures

//        BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_BYTE_GRAY);
//        DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();

        BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
        DataBufferInt buffer = (DataBufferInt) image.getRaster().getDataBuffer();

        for (int x = 0; x < 512; x++) {
            for (int z = 0; z < 512; z++) {
                double doubleVal = heightmap[x][z] * scaleFactor;
                int val = DoubleMath.roundToInt(doubleVal, RoundingMode.HALF_UP);
                buffer.setElem(z * 512 + x, val);
            }
        }

        ImageIO.write(image, "png", new File("platec_heightmap.png"));
    }

    public static float[][] readFile() throws IOException {
        // TODO: Exact file to read has been hard coded in engine for security reasons until height maps become assets
        String file = "Heightmap.txt";
        String delimiter = "\n";

        try (InputStream fis = new FileInputStream(file)) {
            return readValues(fis, delimiter);
        }
    }

    public static float[][] readValues(java.io.InputStream in, String delimiter) throws java.io.IOException, java.lang.NumberFormatException {
        String thisLine;
        int index = 0;
        float min = 0;
        float max = 0;
        float[][] theMap = new float[512][512];

        try (java.io.BufferedInputStream s = new java.io.BufferedInputStream(in);
             java.io.BufferedReader myInput = new java.io.BufferedReader(new java.io.InputStreamReader(s, Charsets.UTF_8))) {
            while ((thisLine = myInput.readLine()) != null) {

                // scan it line by line
                java.util.StringTokenizer st = new java.util.StringTokenizer(thisLine, delimiter);
                float a = Float.valueOf(st.nextToken());
                theMap[index / 512][index % 512] = a;
                index++;
                min = a < min ? a : min;
                max = a > max ? a : max;
                if (index / 512 > 511) {
                    break;
                }
            }
        }

        return (theMap);
    }
}
