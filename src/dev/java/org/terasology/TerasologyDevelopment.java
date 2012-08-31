package org.terasology;

import java.io.File;

public final class TerasologyDevelopment {

    private static final String FOLDER = "dev";

    private TerasologyDevelopment() {
    }

    public static File getOutputFolder(final String subFolderName) {
        return new File(FOLDER, subFolderName);
    }

}
