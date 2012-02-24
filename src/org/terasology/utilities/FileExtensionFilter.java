package org.terasology.utilities;

import java.io.File;
import java.io.FilenameFilter;

public class FileExtensionFilter implements FilenameFilter {
    private String _extension;

    public FileExtensionFilter(String extension) {
        _extension = extension;
    }

    public boolean accept(File dir, String name) {
        return (name.endsWith(_extension));
    }
}