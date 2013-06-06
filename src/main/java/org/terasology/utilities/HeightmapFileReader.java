package org.terasology.utilities;


/**
 * Reads a heightmap encoded in a textfile
 *
 * @author Nym Traveel
 */
public class HeightmapFileReader {

    public static float[][] readFile(String file, String delimiter)
            throws Exception {
        return(readValues(new java.io.FileInputStream(file), delimiter));
    }

    public static float[][] readValues(java.io.InputStream in, String delimiter)
            throws  java.io.FileNotFoundException,
            java.io.IOException,
            java.lang.NumberFormatException {
        String thisLine;
        java.io.BufferedInputStream s = new java.io.BufferedInputStream(in);
        java.io.BufferedReader myInput = new java.io.BufferedReader
                (new java.io.InputStreamReader(s));

        int index = 0;
        float min=0;
        float max=0;
        float[][] theMap = new float[512][512];

        while ((thisLine = myInput.readLine()) != null) {

            // scan it line by line
            java.util.StringTokenizer st = new java.util.StringTokenizer(thisLine, delimiter);
            float a = Float.valueOf(st.nextToken());
            theMap[index/512][index%512] = a;
            index++;
            min = a<min ? a:min;
            max = a>max ? a:max;
            if (index/512>511)break;
        }
        return(theMap);
    }
}
