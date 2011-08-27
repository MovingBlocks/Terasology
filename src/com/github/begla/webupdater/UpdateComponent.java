/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.webupdater;

import com.github.begla.blockmania.utilities.Helper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UpdateComponent {

    private final URL _locationURL;
    private final String _fileName;

    public UpdateComponent(URL _webURL, String _fileName) {
        this._locationURL = _webURL;
        this._fileName = _fileName;
    }

    public String getFileName() {
        return _fileName;
    }

    URL getLocationURL() {
        return _locationURL;
    }

    public URL getURL() {
        try {
            return new URL(getLocationURL().toString() + getFileName());
        } catch (MalformedURLException ex) {
            Helper.LOGGER.log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public String getLocalPath() {
        return "DATA/" + getFileName();
    }
}
