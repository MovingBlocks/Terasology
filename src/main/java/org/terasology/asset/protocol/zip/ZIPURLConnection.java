/*
 * Copyright 2012
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

package org.terasology.asset.protocol.zip;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * @author Immortius
 */
public class ZIPURLConnection extends URLConnection {

    ZIPURLConnection(URL url) throws MalformedURLException {
        super(url);
    }

    @Override
    public void connect() throws IOException {
    }

    @Override
    public InputStream getInputStream() throws IOException {

        InputStream result = null;
        URL url = this.url;

        String spec = url.getFile();
        int separator = spec.indexOf('!');

        // TODO: Do we need to support nested zip?

        if (separator == -1) {
            throw new MalformedURLException("no ! found in url spec:" + spec);
        }

        URL zipFileURL = new URL(spec.substring(0, separator));
        String entryName = spec.substring(separator+2);

        ZipFile zipFile = new ZipFile(zipFileURL.getFile());
        ZipEntry entry = zipFile.getEntry(entryName);
        if (entry != null) {
            return zipFile.getInputStream(entry);
        }
        return null;
    }
}
