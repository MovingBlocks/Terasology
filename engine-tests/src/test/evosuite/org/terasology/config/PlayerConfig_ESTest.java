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
 * Wed Mar 22 21:30:03 GMT 2017
 */

package org.terasology.config;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.terasology.config.PlayerConfig;
import org.terasology.rendering.nui.Color;


public class PlayerConfig_ESTest{


  @Test(timeout = 4000)
  public void testPlayerConfigSetNameNull()  throws Throwable  {
      PlayerConfig playerConfig0 = new PlayerConfig();
      playerConfig0.setName((String) null);
      String string0 = playerConfig0.getName();
      assertNull(string0);
  }


  @Test(timeout = 4000)
  public void testPlayerConfigSetEyeHeightNull()  throws Throwable  {
      PlayerConfig playerConfig0 = new PlayerConfig();
      // Undeclared exception!
      try { 
        playerConfig0.setEyeHeight((Float) null);
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         //assertThrownBy("org.terasology.config.PlayerConfig", e);
      }
  }


  @Test(timeout = 4000)
  public void testPlayerConfigSetName()  throws Throwable  {
      PlayerConfig playerConfig0 = new PlayerConfig();
      playerConfig0.setName("");
      String string0 = playerConfig0.getName();
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void testPlayerConfigSetHasEnteredUsername()  throws Throwable  {
      PlayerConfig playerConfig0 = new PlayerConfig();
      playerConfig0.setHasEnteredUsername(true);
      boolean boolean0 = playerConfig0.hasEnteredUsername();
      assertTrue(boolean0);
  }
}
