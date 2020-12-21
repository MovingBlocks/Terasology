// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.subsystem;

public class DisplayDeviceInfo {

    private String openGlVendor;
    private String openGlVersion;
    private String openGlRenderer;

    public DisplayDeviceInfo(String openGlVendor, String openGlVersion, String openGlRenderer) {
        this.openGlVendor = openGlVendor;
        this.openGlVersion = openGlVersion;
        this.openGlRenderer = openGlRenderer;
    }

    public DisplayDeviceInfo(String unknown) {
        this.openGlVendor = unknown;
        this.openGlVersion = unknown;
        this.openGlRenderer = unknown;
    }

    public String getOpenGlVendor() {
        return openGlVendor;
    }

    public String getOpenGLVersion() {
        return openGlVersion;
    }

    public String getOpenGLRenderer() {
        return openGlRenderer;
    }

    public void setOpenGlVendor(String openGlVendor) {
        this.openGlVendor = openGlVendor;
    }

    public void setOpenGlVersion(String openGlVersion) {
        this.openGlVersion = openGlVersion;
    }

    public void setOpenGlRenderer(String openGlRenderer) {
        this.openGlRenderer = openGlRenderer;
    }
}
