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

/*
 * This file was automatically generated by EvoSuite
 * Wed Mar 22 21:18:33 GMT 2017
 */

package org.terasology.config;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.lwjgl.opengl.PixelFormat;
import org.terasology.config.RenderingConfig;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.rendering.cameras.PerspectiveCameraSettings;
import org.terasology.rendering.nui.layers.mainMenu.videoSettings.DisplayModeSetting;
import org.terasology.rendering.nui.layers.mainMenu.videoSettings.ScreenshotSize;
import org.terasology.rendering.world.viewDistance.ViewDistance;

public class RenderingConfig_ESTest {

  @Test(timeout = 4000)
  public void test000()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setVrSupport(true);
      boolean boolean0 = renderingConfig0.isVrSupport();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test001()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setVolumetricFog(true);
      boolean boolean0 = renderingConfig0.isVolumetricFog();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test002()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setVignette(true);
      boolean boolean0 = renderingConfig0.isVignette();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test003()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setVSync(true);
      boolean boolean0 = renderingConfig0.isVSync();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test004()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setParallaxMapping(true);
      boolean boolean0 = renderingConfig0.isParallaxMapping();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test005()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setNormalMapping(true);
      boolean boolean0 = renderingConfig0.isNormalMapping();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test006()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setLocalReflections(true);
      boolean boolean0 = renderingConfig0.isLocalReflections();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test007()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setDynamicShadowsPcfFiltering(true);
      boolean boolean0 = renderingConfig0.isDynamicShadowsPcfFiltering();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test008()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setDynamicShadows(true);
      boolean boolean0 = renderingConfig0.isDynamicShadows();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test009()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setClampLighting(true);
      boolean boolean0 = renderingConfig0.isClampLighting();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test010()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setCameraBobbing(true);
      boolean boolean0 = renderingConfig0.isCameraBobbing();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test011()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setBloom(true);
      boolean boolean0 = renderingConfig0.isBloom();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test012()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setAnimateWater(true);
      boolean boolean0 = renderingConfig0.isAnimateWater();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test013()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setWindowWidth(2203);
      int int0 = renderingConfig0.getWindowWidth();
      assertEquals(2203, int0);
  }

  @Test(timeout = 4000)
  public void test014()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setWindowPosY(2);
      int int0 = renderingConfig0.getWindowPosY();
      assertEquals(2, int0);
  }

  @Test(timeout = 4000)
  public void test015()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setWindowPosX(2);
      int int0 = renderingConfig0.getWindowPosX();
      assertEquals(2, int0);
  }

  @Test(timeout = 4000)
  public void test016()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setWindowHeight(736);
      int int0 = renderingConfig0.getWindowHeight();
      assertEquals(736, int0);
  }

  @Test(timeout = 4000)
  public void test017()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setWindowHeight((-1));
      int int0 = renderingConfig0.getWindowHeight();
      assertEquals((-1), int0);
  }

  @Test(timeout = 4000)
  public void test018()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      ViewDistance viewDistance0 = ViewDistance.LEGALLY_BLIND;
      renderingConfig0.setViewDistance(viewDistance0);
      ViewDistance viewDistance1 = renderingConfig0.getViewDistance();
      assertEquals(0, viewDistance1.getIndex());
  }

  @Test(timeout = 4000)
  public void test019()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setShadowMapResolution(2704);
      int int0 = renderingConfig0.getShadowMapResolution();
      assertEquals(2704, int0);
  }

  @Test(timeout = 4000)
  public void test020()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setShadowMapResolution((-1186));
      int int0 = renderingConfig0.getShadowMapResolution();
      assertEquals((-1186), int0);
  }

  @Test(timeout = 4000)
  public void test021()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      ScreenshotSize screenshotSize0 = ScreenshotSize.HD1080;
      renderingConfig0.setScreenshotSize(screenshotSize0);
      ScreenshotSize screenshotSize1 = renderingConfig0.getScreenshotSize();
      assertSame(screenshotSize1, screenshotSize0);
  }

  @Test(timeout = 4000)
  public void test022()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      PixelFormat pixelFormat0 = new PixelFormat(12, 1, 1);
      PixelFormat pixelFormat1 = pixelFormat0.withSRGB(true);
      renderingConfig0.setPixelFormat(pixelFormat1);
      PixelFormat pixelFormat2 = renderingConfig0.getPixelFormat();
      assertEquals(0, pixelFormat2.getAccumulationBitsPerPixel());
  }

  @Test(timeout = 4000)
  public void test023()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      PixelFormat pixelFormat0 = new PixelFormat(3, 0, (-144), (-1162));
      renderingConfig0.setPixelFormat(pixelFormat0);
      PixelFormat pixelFormat1 = renderingConfig0.getPixelFormat();
      assertEquals(0, pixelFormat1.getDepthBits());
  }

  @Test(timeout = 4000)
  public void test024()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      PixelFormat pixelFormat0 = new PixelFormat(4177, 3, 4177, 1, 3, (-1), 277, 1, false);
      renderingConfig0.setPixelFormat(pixelFormat0);
      PixelFormat pixelFormat1 = renderingConfig0.getPixelFormat();
      assertEquals(3, pixelFormat1.getSamples());
  }

  @Test(timeout = 4000)
  public void test025()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      PixelFormat pixelFormat0 = new PixelFormat();
      renderingConfig0.setPixelFormat(pixelFormat0);
      PixelFormat pixelFormat1 = renderingConfig0.getPixelFormat();
      assertEquals(0, pixelFormat1.getStencilBits());
  }

  @Test(timeout = 4000)
  public void test026()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      PixelFormat pixelFormat0 = new PixelFormat(2, 2, 2, 2, 2, 0, (-4903), 2, false);
      renderingConfig0.setPixelFormat(pixelFormat0);
      PixelFormat pixelFormat1 = renderingConfig0.getPixelFormat();
      assertSame(pixelFormat1, pixelFormat0);
  }

  @Test(timeout = 4000)
  public void test027()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      PixelFormat pixelFormat0 = new PixelFormat((-1230), (-1230), 308);
      renderingConfig0.setPixelFormat(pixelFormat0);
      PixelFormat pixelFormat1 = renderingConfig0.getPixelFormat();
      assertEquals(0, pixelFormat1.getSamples());
  }

  @Test(timeout = 4000)
  public void test028()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      PixelFormat pixelFormat0 = new PixelFormat((-274), (-274), 4550, (-274), 3561, 4550, 3561, 3, false);
      renderingConfig0.setPixelFormat(pixelFormat0);
      PixelFormat pixelFormat1 = renderingConfig0.getPixelFormat();
      assertEquals(4550, pixelFormat1.getAuxBuffers());
  }

  @Test(timeout = 4000)
  public void test029()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      PixelFormat pixelFormat0 = new PixelFormat(3, 340, (-1039), 0, 3, 123, 123, (-2725), true, true);
      renderingConfig0.setPixelFormat(pixelFormat0);
      PixelFormat pixelFormat1 = renderingConfig0.getPixelFormat();
      assertEquals(123, pixelFormat1.getAccumulationBitsPerPixel());
  }

  @Test(timeout = 4000)
  public void test030()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setParticleEffectLimit(1927);
      int int0 = renderingConfig0.getParticleEffectLimit();
      assertEquals(1927, int0);
  }

  @Test(timeout = 4000)
  public void test031()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setParticleEffectLimit((-1));
      int int0 = renderingConfig0.getParticleEffectLimit();
      assertEquals((-1), int0);
  }

  @Test(timeout = 4000)
  public void test032()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setMeshLimit(217);
      int int0 = renderingConfig0.getMeshLimit();
      assertEquals(217, int0);
  }

  @Test(timeout = 4000)
  public void test033()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setMeshLimit((-1417));
      int int0 = renderingConfig0.getMeshLimit();
      assertEquals((-1417), int0);
  }

  @Test(timeout = 4000)
  public void test034()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setMaxTextureAtlasResolution(2);
      int int0 = renderingConfig0.getMaxTextureAtlasResolution();
      assertEquals(2, int0);
  }

  @Test(timeout = 4000)
  public void test035()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setMaxTextureAtlasResolution((-2512));
      int int0 = renderingConfig0.getMaxTextureAtlasResolution();
      assertEquals((-2512), int0);
  }

  @Test(timeout = 4000)
  public void test036()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setMaxChunksUsedForShadowMapping(2);
      int int0 = renderingConfig0.getMaxChunksUsedForShadowMapping();
      assertEquals(2, int0);
  }

  @Test(timeout = 4000)
  public void test037()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setMaxChunksUsedForShadowMapping((-2283));
      int int0 = renderingConfig0.getMaxChunksUsedForShadowMapping();
      assertEquals((-2283), int0);
  }

  @Test(timeout = 4000)
  public void test038()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setFrameLimit(1403641);
      int int0 = renderingConfig0.getFrameLimit();
      assertEquals(1403641, int0);
  }

  @Test(timeout = 4000)
  public void test039()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setFrameLimit((-1940));
      int int0 = renderingConfig0.getFrameLimit();
      assertEquals((-1940), int0);
  }

  @Test(timeout = 4000)
  public void test040()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setFieldOfView((-131.6F));
      float float0 = renderingConfig0.getFieldOfView();
      assertEquals((-131.6F), float0, 0.01F);
  }

  @Test(timeout = 4000)
  public void test041()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setFboScale(1055);
      int int0 = renderingConfig0.getFboScale();
      assertEquals(1055, int0);
  }

  @Test(timeout = 4000)
  public void test042()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setFullscreen(true);
      DisplayModeSetting displayModeSetting0 = renderingConfig0.getDisplayModeSetting();
      assertTrue(displayModeSetting0.isCurrent());
      assertEquals("${engine:menu#video-fullscreen}", displayModeSetting0.toString());
  }

  @Test(timeout = 4000)
  public void test043()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setWindowWidth(369);
      renderingConfig0.getDisplayMode();
      assertEquals(369, renderingConfig0.getWindowWidth());
  }

  @Test(timeout = 4000)
  public void test044()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setWindowWidth((-2032));
      renderingConfig0.getDisplayMode();
      assertEquals(-2032, renderingConfig0.getWindowWidth());
  }

  @Test(timeout = 4000)
  public void test045()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setWindowHeight(1);
      renderingConfig0.getDisplayMode();
      assertEquals(1, renderingConfig0.getWindowHeight());
  }

  @Test(timeout = 4000)
  public void test046()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setBlurIntensity(900);
      int int0 = renderingConfig0.getBlurIntensity();
      assertEquals(900, renderingConfig0.getBlurRadius());
      assertEquals(900, int0);
  }

  @Test(timeout = 4000)
  public void test047()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      // Undeclared exception!
      try { 
        renderingConfig0.setScreenshotFormat("q;PL(eA~n9uJ9[");
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
      }
  }

  @Test(timeout = 4000)
  public void test048()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      // Undeclared exception!
      try { 
        renderingConfig0.setDisplayModeSetting((DisplayModeSetting) null);
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         //assertThrownBy("org.terasology.config.RenderingConfig", e);
      }
  }

  @Test(timeout = 4000)
  public void test049()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      // Undeclared exception!
      try { 
        renderingConfig0.isWindowedFullscreen();
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         //assertThrownBy("org.terasology.config.RenderingConfig", e);
      }
  }

  @Test(timeout = 4000)
  public void test050()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setFullscreen(false);
      assertFalse(renderingConfig0.isFullscreen());
      
      RenderingConfig renderingConfig1 = new RenderingConfig();
      DisplayModeSetting displayModeSetting0 = DisplayModeSetting.FULLSCREEN;
      renderingConfig1.setDisplayModeSetting(displayModeSetting0);
      DisplayModeSetting displayModeSetting1 = renderingConfig0.getDisplayModeSetting();
      assertFalse(displayModeSetting1.isCurrent());
  }

  @Test(timeout = 4000)
  public void test051()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isMotionBlur();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test052()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      ViewDistance viewDistance0 = renderingConfig0.getViewDistance();
      assertEquals(2, viewDistance0.getIndex());
  }

  @Test(timeout = 4000)
  public void test053()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setWindowedFullscreen(true);
      boolean boolean0 = renderingConfig0.isWindowedFullscreen();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test054()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setWindowedFullscreen(false);
      boolean boolean0 = renderingConfig0.isWindowedFullscreen();
      assertFalse(boolean0);
      assertTrue(renderingConfig0.isFullscreen());
  }

  @Test(timeout = 4000)
  public void test055()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setFullscreen(true);
      boolean boolean0 = renderingConfig0.isFullscreen();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test056()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setWindowedFullscreen(true);
      boolean boolean0 = renderingConfig0.isFullscreen();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test057()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      DisplayModeSetting displayModeSetting0 = DisplayModeSetting.WINDOWED_FULLSCREEN;
      renderingConfig0.setDisplayModeSetting(displayModeSetting0);
      assertFalse(renderingConfig0.isFullscreen());
  }

  @Test(timeout = 4000)
  public void test058()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      DisplayModeSetting displayModeSetting0 = DisplayModeSetting.WINDOWED;
      renderingConfig0.setDisplayModeSetting(displayModeSetting0);
      assertFalse(renderingConfig0.isFullscreen());
  }

  @Test(timeout = 4000)
  public void test059()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.getDisplayModeSetting();
  }

  @Test(timeout = 4000)
  public void test060()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isRenderNearest();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test061()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isEyeAdaptation();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test062()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isParallaxMapping();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test063()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setAnimatedMenu(true);
      boolean boolean0 = renderingConfig0.isAnimatedMenu();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test064()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setDumpShaders(true);
      boolean boolean0 = renderingConfig0.isDumpShaders();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test065()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setRenderNearest(true);
      boolean boolean0 = renderingConfig0.isRenderNearest();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test066()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isLightShafts();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test067()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      int int0 = renderingConfig0.getWindowPosY();
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test068()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setFieldOfView(12);
      float float0 = renderingConfig0.getFieldOfView();
      assertEquals(12.0F, float0, 0.01F);
  }

  @Test(timeout = 4000)
  public void test069()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      int int0 = renderingConfig0.getMeshLimit();
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test070()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      String string0 = renderingConfig0.getScreenshotFormat();
      assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test071()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setEyeAdaptation(true);
      boolean boolean0 = renderingConfig0.isEyeAdaptation();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test072()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.getScreenshotSize();
  }

  @Test(timeout = 4000)
  public void test073()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isRenderPlacingBox();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test074()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setFlickeringLight(true);
      boolean boolean0 = renderingConfig0.isFlickeringLight();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test075()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isReflectiveWater();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test076()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isAnimateGrass();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test077()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isLocalReflections();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test078()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isNormalMapping();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test079()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setCloudShadows(true);
      boolean boolean0 = renderingConfig0.isCloudShadows();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test080()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isVrSupport();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test081()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      int int0 = renderingConfig0.getWindowWidth();
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test082()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isClampLighting();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test083()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isBloom();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test084()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isDynamicShadows();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test085()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isVSync();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test086()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isDumpShaders();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test087()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isAnimateWater();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test088()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      int int0 = renderingConfig0.getParticleEffectLimit();
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test089()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      int int0 = renderingConfig0.getWindowPosX();
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test090()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isDynamicShadowsPcfFiltering();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test091()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isOutline();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test092()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      ScreenshotSize screenshotSize0 = ScreenshotSize.DOUBLE_SIZE;
      renderingConfig0.setScreenshotSize(screenshotSize0);
      ScreenshotSize screenshotSize1 = renderingConfig0.getScreenshotSize();
      assertEquals("${engine:menu#screenshot-size-double}", screenshotSize1.toString());
  }

  @Test(timeout = 4000)
  public void test093()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isFilmGrain();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test094()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isSsao();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test095()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setWindowWidth((-798));
      int int0 = renderingConfig0.getWindowWidth();
      assertEquals((-798), int0);
  }

  @Test(timeout = 4000)
  public void test096()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      int int0 = renderingConfig0.getMaxTextureAtlasResolution();
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test097()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      int int0 = renderingConfig0.getBlurIntensity();
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test098()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isVolumetricFog();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test099()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      int int0 = renderingConfig0.getMaxChunksUsedForShadowMapping();
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test100()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      int int0 = renderingConfig0.getBlurRadius();
      assertEquals(1, int0);
  }

  @Test(timeout = 4000)
  public void test101()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setInscattering(true);
      boolean boolean0 = renderingConfig0.isInscattering();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test102()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setSsao(true);
      boolean boolean0 = renderingConfig0.isSsao();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test103()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isAnimatedMenu();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test104()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isVignette();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test105()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      int int0 = renderingConfig0.getWindowHeight();
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test106()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setVrSupport(true);
      renderingConfig0.setMotionBlur(true);
      boolean boolean0 = renderingConfig0.isMotionBlur();
      assertTrue(renderingConfig0.isVrSupport());
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test107()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isFlickeringLight();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test108()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      PerspectiveCameraSettings perspectiveCameraSettings0 = renderingConfig0.getCameraSettings();
      assertNull(perspectiveCameraSettings0);
  }

  @Test(timeout = 4000)
  public void test109()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setAnimateGrass(true);
      boolean boolean0 = renderingConfig0.isAnimateGrass();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test110()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setFilmGrain(true);
      boolean boolean0 = renderingConfig0.isFilmGrain();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test111()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      int int0 = renderingConfig0.getFboScale();
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test112()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      int int0 = renderingConfig0.getFrameLimit();
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test113()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isInscattering();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test114()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setFboScale((-1399));
      int int0 = renderingConfig0.getFboScale();
      assertEquals((-1399), int0);
  }

  @Test(timeout = 4000)
  public void test115()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setWindowHeight((-3056));
      renderingConfig0.getDisplayMode();
      assertEquals(-3056, renderingConfig0.getWindowHeight());
  }

  @Test(timeout = 4000)
  public void test116()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setWindowPosY((-8));
      int int0 = renderingConfig0.getWindowPosY();
      assertEquals((-8), int0);
  }

  @Test(timeout = 4000)
  public void test117()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setLightShafts(true);
      boolean boolean0 = renderingConfig0.isLightShafts();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test118()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      PixelFormat pixelFormat0 = renderingConfig0.getPixelFormat();
      assertNull(pixelFormat0);
  }

  @Test(timeout = 4000)
  public void test119()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setWindowPosX((-1586));
      int int0 = renderingConfig0.getWindowPosX();
      assertEquals((-1586), int0);
  }

  @Test(timeout = 4000)
  public void test120()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      RenderingDebugConfig renderingDebugConfig0 = renderingConfig0.getDebug();
      assertFalse(renderingDebugConfig0.isWireframe());
  }

  @Test(timeout = 4000)
  public void test121()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setOutline(true);
      boolean boolean0 = renderingConfig0.isOutline();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test122()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setMotionBlur(true);
      boolean boolean0 = renderingConfig0.isMotionBlur();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test123()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      float float0 = renderingConfig0.getFieldOfView();
      assertEquals(0.0F, float0, 0.01F);
  }

  @Test(timeout = 4000)
  public void test124()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isCameraBobbing();
      assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test125()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      int int0 = renderingConfig0.getShadowMapResolution();
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test126()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setReflectiveWater(true);
      boolean boolean0 = renderingConfig0.isReflectiveWater();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test127()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setRenderPlacingBox(true);
      boolean boolean0 = renderingConfig0.isRenderPlacingBox();
      assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test128()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      renderingConfig0.setBlurIntensity((-5905));
      int int0 = renderingConfig0.getBlurIntensity();
      assertEquals((-5905), int0);
  }

  @Test(timeout = 4000)
  public void test129()  throws Throwable  {
      RenderingConfig renderingConfig0 = new RenderingConfig();
      boolean boolean0 = renderingConfig0.isCloudShadows();
      assertFalse(boolean0);
  }
}
