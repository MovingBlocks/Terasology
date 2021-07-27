// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config;

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
