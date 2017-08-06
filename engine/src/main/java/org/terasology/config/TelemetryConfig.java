/*
 * Copyright 2017 MovingBlocks
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

/**
 *  Configuration (authorisation) for telemetry system.
 */
public class TelemetryConfig {

    private boolean telemetryEnabled;

    private String telemetryDestination;

    private String telemetryServerName;

    private String telemetryServerOwner;

    private boolean errorReportingEnabled;

    private String errorReportingDestination;

    private String errorReportingServerName;

    private String errorReportingServerOwner;

    private boolean launchPopupDisabled;

    public boolean isTelemetryEnabled() {
        return telemetryEnabled;
    }

    private MetricsUserPermissionConfig metricsUserPermissionConfig = new MetricsUserPermissionConfig();

    public MetricsUserPermissionConfig getMetricsUserPermissionConfig() {
        return metricsUserPermissionConfig;
    }

    public void setMetricsUserPermissionConfig(MetricsUserPermissionConfig metricsUserPermissionConfig) {
        this.metricsUserPermissionConfig = metricsUserPermissionConfig;
    }

    public void setTelemetryEnabled(boolean telemetryEnabled) {
        this.telemetryEnabled = telemetryEnabled;
    }

    public String getTelemetryDestination() {
        return telemetryDestination;
    }

    public void setTelemetryDestination(String telemetryDestination) {
        this.telemetryDestination = telemetryDestination;
    }

    public String getTelemetryServerName() {
        return telemetryServerName;
    }

    public void setTelemetryServerName(String telemetryServerName) {
        this.telemetryServerName = telemetryServerName;
    }

    public String getTelemetryServerOwner() {
        return telemetryServerOwner;
    }

    public void setTelemetryServerOwner(String telemetryServerOwner) {
        this.telemetryServerOwner = telemetryServerOwner;
    }

    public boolean isErrorReportingEnabled() {
        return errorReportingEnabled;
    }

    public void setErrorReportingEnabled(boolean errorReportingEnabled) {
        this.errorReportingEnabled = errorReportingEnabled;
    }

    public String getErrorReportingDestination() {
        return errorReportingDestination;
    }

    public void setErrorReportingDestination(String errorReportingDestination) {
        this.errorReportingDestination = errorReportingDestination;
    }

    public String getErrorReportingServerName() {
        return errorReportingServerName;
    }

    public void setErrorReportingServerName(String errorReportingServerName) {
        this.errorReportingServerName = errorReportingServerName;
    }

    public String getErrorReportingServerOwner() {
        return errorReportingServerOwner;
    }

    public void setErrorReportingServerOwner(String errorReportingServerOwner) {
        this.errorReportingServerOwner = errorReportingServerOwner;
    }

    public boolean isLaunchPopupDisabled() {
        return launchPopupDisabled;
    }

    public void setLaunchPopupDisabled(boolean launchPopupDisabled) {
        this.launchPopupDisabled = launchPopupDisabled;
    }

    public void setTelemetryAndErrorReportingEnable(boolean enabled) {
        telemetryEnabled = enabled;
        errorReportingEnabled = enabled;
    }
}
