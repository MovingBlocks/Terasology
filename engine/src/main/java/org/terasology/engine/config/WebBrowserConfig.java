/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.config;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Holds all the trusted URLs and HostNames.
 */
public class WebBrowserConfig {

    private List<String> trustedUrls = Lists.newArrayList();
    private List<String> trustedHostNames = Lists.newArrayList();

    public void addTrustedUrls(String url) {
        trustedUrls.add(url);
    }

    public void removeTrustedUrl(String url) {
        trustedUrls.remove(url);
    }

    public boolean isUrlTrusted(String url) {
        return trustedUrls.contains(url);
    }

    public List<String> getTrustedUrls() {
        return trustedUrls;
    }

    public void addTrustedHostName(String hostname) {
        trustedHostNames.add(hostname);
    }

    public void removeTrustedHostName(String hostname) {
        trustedHostNames.remove(hostname);
    }

    public boolean isHostNameTrusted(String hostname) {
        return trustedHostNames.contains(hostname);
    }

    public List<String> getTrustedHostNames() {
        return trustedHostNames;
    }
}
