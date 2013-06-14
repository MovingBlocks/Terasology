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

package org.terasology.rendering.oculusVr;

/**
 * Helper class for the Oculus Rift.
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class OculusVrHelper {

    public static final float ocVerticalRes = 800.0f;
    public static final float ocHorizontalRes = 1280.0f;
    public static final float ocVerticalScreenSize = 0.0935f;
    public static final float ocHorizontalScreenSize = 0.14976f;
    public static final float ocEyeToScreenDistance = 0.041f;
    public static final float ocLensSeparationDistance = 0.0635f;
    public static final float ocInterpupillaryDistance = 0.064f;

    public static final float ocHalfScreenDistance = ocHorizontalScreenSize * 0.5f;

    public static final float ocAspectRatio = (ocHorizontalRes * 0.5f) / ocVerticalRes;

    public static float ocViewCenter = ocHorizontalScreenSize * 0.25f;
    public static float ocEyeProjectionShift = ocViewCenter - ocLensSeparationDistance * 0.5f;
    public static float ocProjectionCenterOffset = 4.0f * ocEyeProjectionShift / ocHorizontalScreenSize;

    public static float ocLensOffset = ocLensSeparationDistance * 0.5f;
    public static float ocLensShift = ocHorizontalScreenSize * 0.25f - ocLensOffset;
    public static float ocLensViewportShift = 4.0f * ocLensShift / ocHorizontalScreenSize;

    public static final float ocScaleFactor = 1.6f;

    public static final float ocPercievedHalfRTDistance = (ocVerticalScreenSize / 2) * ocScaleFactor;
    public static final float ocYFov = 2.0f * (float) Math.atan(ocPercievedHalfRTDistance/ocEyeToScreenDistance);
    //public static final float ocYFov = 2.0f * (float) Math.atan(ocHalfScreenDistance/ocEyeToScreenDistance);

    public static final float ocDistortionParams[] = { 1.0f, 0.22f, 0.24f, 0.0f };
}
