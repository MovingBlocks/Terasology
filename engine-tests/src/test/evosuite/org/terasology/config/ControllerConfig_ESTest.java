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
 * Wed Mar 22 21:28:56 GMT 2017
 */

package org.terasology.config;

import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.terasology.config.ControllerConfig;


public class ControllerConfig_ESTest {

  @Test(timeout = 4000)
  public void testControllerConfigIsInvertZ()  throws Throwable  {
      ControllerConfig controllerConfig0 = new ControllerConfig();
      ControllerConfig.ControllerInfo controllerConfig_ControllerInfo0 = controllerConfig0.getController("");
      assertTrue(controllerConfig_ControllerInfo0.isInvertZ());
      
      controllerConfig_ControllerInfo0.setInvertZ(false);
      assertFalse(controllerConfig_ControllerInfo0.isInvertZ());
      
      ControllerConfig.ControllerInfo controllerConfig_ControllerInfo1 = controllerConfig0.getController("");
      assertEquals(0.08F, controllerConfig_ControllerInfo1.getMovementDeadZone(), 0.01F);
  }

  @Test(timeout = 4000)
  public void testControllerConfigIsInvertY()  throws Throwable  {
      ControllerConfig controllerConfig0 = new ControllerConfig();
      ControllerConfig.ControllerInfo controllerConfig_ControllerInfo0 = controllerConfig0.getController("");
      assertTrue(controllerConfig_ControllerInfo0.isInvertY());
      
      controllerConfig_ControllerInfo0.setInvertY(false);
      assertFalse(controllerConfig_ControllerInfo0.isInvertY());
      
      ControllerConfig.ControllerInfo controllerConfig_ControllerInfo1 = controllerConfig0.getController("");
      assertFalse(controllerConfig_ControllerInfo1.isInvertY());
  }

  @Test(timeout = 4000)
  public void testControllerConfigIsInvertX()  throws Throwable  {
      ControllerConfig controllerConfig0 = new ControllerConfig();
      ControllerConfig.ControllerInfo controllerConfig_ControllerInfo0 = controllerConfig0.getController("");
      assertTrue(controllerConfig_ControllerInfo0.isInvertX());
      
      controllerConfig_ControllerInfo0.setInvertX(false);
      assertFalse(controllerConfig_ControllerInfo0.isInvertX());
      
      ControllerConfig.ControllerInfo controllerConfig_ControllerInfo1 = controllerConfig0.getController("");
      assertEquals(0.08F, controllerConfig_ControllerInfo1.getRotationDeadZone(), 0.01F);
  }

  @Test(timeout = 4000)
  public void testControllerConfigGetRotationDeadZone1()  throws Throwable  {
      ControllerConfig controllerConfig0 = new ControllerConfig();
      ControllerConfig.ControllerInfo controllerConfig_ControllerInfo0 = controllerConfig0.getController("x");
      controllerConfig_ControllerInfo0.setRotationDeadZone((-279.62115F));
      assertEquals((-279.62115F), controllerConfig_ControllerInfo0.getRotationDeadZone(), 0.01F);
      
      ControllerConfig.ControllerInfo controllerConfig_ControllerInfo1 = controllerConfig0.getController("x");
      assertEquals(0.08F, controllerConfig_ControllerInfo1.getMovementDeadZone(), 0.01F);
  }

  @Test(timeout = 4000)
  public void testControllerConfigGetRotationDeadZone2()  throws Throwable  {
      ControllerConfig controllerConfig0 = new ControllerConfig();
      ControllerConfig.ControllerInfo controllerConfig_ControllerInfo0 = controllerConfig0.getController("");
      assertEquals(0.08F, controllerConfig_ControllerInfo0.getMovementDeadZone(), 0.01F);
      
      controllerConfig_ControllerInfo0.setMovementDeadZone(0.0F);
      ControllerConfig.ControllerInfo controllerConfig_ControllerInfo1 = controllerConfig0.getController("");
      assertEquals(0.08F, controllerConfig_ControllerInfo1.getRotationDeadZone(), 0.01F);
  }

  @Test(timeout = 4000)
  public void testControllerConfigGetRotationDeadZone3()  throws Throwable  {
      ControllerConfig controllerConfig0 = new ControllerConfig();
      ControllerConfig.ControllerInfo controllerConfig_ControllerInfo0 = controllerConfig0.getController("");
      controllerConfig_ControllerInfo0.setMovementDeadZone((-1310.0144F));
      assertEquals((-1310.0144F), controllerConfig_ControllerInfo0.getMovementDeadZone(), 0.01F);
      
      ControllerConfig.ControllerInfo controllerConfig_ControllerInfo1 = controllerConfig0.getController("");
      assertEquals(0.08F, controllerConfig_ControllerInfo1.getRotationDeadZone(), 0.01F);
  }

  @Test(timeout = 4000)
  public void testControllerConfigGetRotationDeadZone4()  throws Throwable  {
      ControllerConfig.ControllerInfo controllerConfig_ControllerInfo0 = new ControllerConfig.ControllerInfo();
      boolean boolean0 = controllerConfig_ControllerInfo0.isInvertZ();
      assertTrue(controllerConfig_ControllerInfo0.isInvertY());
      assertTrue(boolean0);
      assertTrue(controllerConfig_ControllerInfo0.isInvertX());
      assertEquals(0.08F, controllerConfig_ControllerInfo0.getRotationDeadZone(), 0.01F);
      assertEquals(0.08F, controllerConfig_ControllerInfo0.getMovementDeadZone(), 0.01F);
  }

  @Test(timeout = 4000)
  public void testControllerConfigGetRotationDeadZone5()  throws Throwable  {
      ControllerConfig.ControllerInfo controllerConfig_ControllerInfo0 = new ControllerConfig.ControllerInfo();
      boolean boolean0 = controllerConfig_ControllerInfo0.isInvertY();
      assertEquals(0.08F, controllerConfig_ControllerInfo0.getMovementDeadZone(), 0.01F);
      assertEquals(0.08F, controllerConfig_ControllerInfo0.getRotationDeadZone(), 0.01F);
      assertTrue(controllerConfig_ControllerInfo0.isInvertX());
      assertTrue(boolean0);
      assertTrue(controllerConfig_ControllerInfo0.isInvertZ());
  }

  @Test(timeout = 4000)
  public void testControllerConfigGetRotationDeadZone6()  throws Throwable  {
      ControllerConfig controllerConfig0 = new ControllerConfig();
      ControllerConfig.ControllerInfo controllerConfig_ControllerInfo0 = controllerConfig0.getController("");
      float float0 = controllerConfig_ControllerInfo0.getMovementDeadZone();
      assertEquals(0.08F, float0, 0.01F);
      assertTrue(controllerConfig_ControllerInfo0.isInvertZ());
      assertTrue(controllerConfig_ControllerInfo0.isInvertX());
      assertTrue(controllerConfig_ControllerInfo0.isInvertY());
      assertEquals(0.08F, controllerConfig_ControllerInfo0.getRotationDeadZone(), 0.01F);
  }

  @Test(timeout = 4000)
  public void testControllerConfigGetRotationDeadZone7()  throws Throwable  {
      ControllerConfig controllerConfig0 = new ControllerConfig();
      ControllerConfig.ControllerInfo controllerConfig_ControllerInfo0 = controllerConfig0.getController("");
      assertEquals(0.08F, controllerConfig_ControllerInfo0.getRotationDeadZone(), 0.01F);
      
      controllerConfig_ControllerInfo0.setRotationDeadZone(0.0F);
      ControllerConfig.ControllerInfo controllerConfig_ControllerInfo1 = controllerConfig0.getController("");
      assertEquals(0.08F, controllerConfig_ControllerInfo1.getMovementDeadZone(), 0.01F);
  }

  @Test(timeout = 4000)
  public void testControllerConfigGetRotationDeadZone8()  throws Throwable  {
      ControllerConfig controllerConfig0 = new ControllerConfig();
      ControllerConfig.ControllerInfo controllerConfig_ControllerInfo0 = controllerConfig0.getController("");
      boolean boolean0 = controllerConfig_ControllerInfo0.isInvertX();
      assertEquals(0.08F, controllerConfig_ControllerInfo0.getMovementDeadZone(), 0.01F);
      assertTrue(boolean0);
      assertEquals(0.08F, controllerConfig_ControllerInfo0.getRotationDeadZone(), 0.01F);
      assertTrue(controllerConfig_ControllerInfo0.isInvertZ());
      assertTrue(controllerConfig_ControllerInfo0.isInvertY());
  }

  @Test(timeout = 4000)
  public void testControllerConfigGetRotationDeadZone9()  throws Throwable  {
      ControllerConfig controllerConfig0 = new ControllerConfig();
      ControllerConfig.ControllerInfo controllerConfig_ControllerInfo0 = controllerConfig0.getController("");
      assertNotNull(controllerConfig_ControllerInfo0);
      
      float float0 = controllerConfig_ControllerInfo0.getRotationDeadZone();
      assertTrue(controllerConfig_ControllerInfo0.isInvertZ());
      assertTrue(controllerConfig_ControllerInfo0.isInvertY());
      assertTrue(controllerConfig_ControllerInfo0.isInvertX());
      assertEquals(0.08F, float0, 0.01F);
      assertEquals(0.08F, controllerConfig_ControllerInfo0.getMovementDeadZone(), 0.01F);
  }
}
